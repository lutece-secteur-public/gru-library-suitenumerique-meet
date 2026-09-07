/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.meet.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.appointment.modules.virtualmeeting.provider.IVirtualMeetingProvider;
import fr.paris.lutece.plugins.meet.business.MeetRoom;
import fr.paris.lutece.plugins.meet.business.MeetRoomRequest;
import fr.paris.lutece.plugins.meet.business.RoomAccessLevel;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

/**
 * Meet provider implementation of {@link IVirtualMeetingProvider}.
 * <p>
 * Integrates with the Meet external API for room management. Since Meet uses room URLs for access (not per-participant tokens), the token generation methods
 * return the room URL.
 * </p>
 */
public class MeetServerService implements IVirtualMeetingProvider
{
    private static final String PROPERTY_BASE_URL = "meet.server.url";
    private static final String PROPERTY_CLIENT_ID = "meet.server.clientId";
    private static final String PROPERTY_CLIENT_SECRET = "meet.server.clientSecret";
    private static final String PROPERTY_SCOPE = "meet.server.scope";
    private static final String PROPERTY_MEETING_URL_PATTERN = "meet.server.meetingUrlPattern";
    private static final String PROPERTY_ACCESS_LEVEL = "meet.server.accessLevel";

    private static final String DEFAULT_MEETING_URL_PATTERN = "https://meet.example.com/{slug}";

    /** Access level the Meet server applies when the field is omitted, as documented in its {@code openapi.yaml}. Informative only — never sent. */
    private static final RoomAccessLevel SERVER_DEFAULT_ACCESS_LEVEL = RoomAccessLevel.TRUSTED;
    private static final String PLACEHOLDER_SLUG = "{slug}";

    private String _strName;
    private boolean _bDefault;

    // Configurable fields — when set, they take priority over AppPropertiesService
    private String _strBaseUrl;
    private String _strClientId;
    private String _strClientSecret;
    private String _strScope;
    private String _strMeetingUrlPattern;
    private String _strAccessLevel;

    private final ConcurrentHashMap<String, MeetRoom> _roomCache = new ConcurrentHashMap<>( );
    private final MeetApiClient _apiClient = new MeetApiClient( );

    // IVirtualMeetingProvider — identity

    public MeetServerService( String providerName, String isDefault )
    {
        _strName = providerName;
        _bDefault = isDefault.isEmpty() ? false : Boolean.parseBoolean( "isDefault" );
    }

    @Override
    public String getName( )
    {
        return _strName;
    }

    public void setName( String strName )
    {
        _strName = strName;
    }

    @Override
    public boolean isDefault( )
    {
        return _bDefault;
    }

    public void setDefault( boolean bDefault )
    {
        _bDefault = bDefault;
    }

    // Configuration setters

    public void setBaseUrl( String strBaseUrl )
    {
        _strBaseUrl = strBaseUrl;
    }

    public void setClientId( String strClientId )
    {
        _strClientId = strClientId;
    }

    public void setClientSecret( String strClientSecret )
    {
        _strClientSecret = strClientSecret;
    }

    public void setScope( String strScope )
    {
        _strScope = strScope;
    }

    public void setMeetingUrlPattern( String strMeetingUrlPattern )
    {
        _strMeetingUrlPattern = strMeetingUrlPattern;
    }

    public void setAccessLevel( String strAccessLevel )
    {
        _strAccessLevel = strAccessLevel;
    }

    // Configuration helpers — field value takes priority, AppPropertiesService is the fallback

    private String getBaseUrl( )
    {
        return _strBaseUrl != null ? _strBaseUrl : AppPropertiesService.getProperty( PROPERTY_BASE_URL, "" );
    }

    private String getClientId( )
    {
        return _strClientId != null ? _strClientId : AppPropertiesService.getProperty( PROPERTY_CLIENT_ID );
    }

    private String getClientSecret( )
    {
        return _strClientSecret != null ? _strClientSecret : AppPropertiesService.getProperty( PROPERTY_CLIENT_SECRET );
    }

    private String getScope( )
    {
        return _strScope != null ? _strScope : AppPropertiesService.getProperty( PROPERTY_SCOPE );
    }

    private String getMeetingUrlPattern( )
    {
        return _strMeetingUrlPattern != null ? _strMeetingUrlPattern : AppPropertiesService.getProperty( PROPERTY_MEETING_URL_PATTERN, DEFAULT_MEETING_URL_PATTERN );
    }

    /**
     * The site-wide default access level. Empty or {@code null} means "let the Meet server decide".
     */
    private String getAccessLevel( )
    {
        return _strAccessLevel != null ? _strAccessLevel : AppPropertiesService.getProperty( PROPERTY_ACCESS_LEVEL );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSupportedAccessLevels( )
    {
        return RoomAccessLevel.getValues( );
    }

    /**
     * {@inheritDoc}
     * <p>
     * Resolves exactly like {@link #createRoom}: the valid site-wide default if one is configured, otherwise the documented Meet server default. An invalid
     * configured value therefore reports the server default, which is what the server will actually apply.
     * </p>
     */
    @Override
    public String getDefaultAccessLevel( )
    {
        RoomAccessLevel level = RoomAccessLevel.fromValue( getAccessLevel( ) );

        return ( level != null ? level : SERVER_DEFAULT_ACCESS_LEVEL ).getValue( );
    }

    // Room management

    /**
     * {@inheritDoc}
     * <p>
     * The Meet API auto-generates room slugs; the {@code roomName} parameter is used as a local cache key only. Parameters {@code emptyTimeout} and
     * {@code maxParticipants} are not supported by the Meet API and are ignored if present.
     * </p>
     * <p>
     * {@code accessLevel} is resolved in three steps: the value passed in {@code mapParameters}, else the site-wide default
     * ({@code meet.server.accessLevel} or the injected field), else nothing — in which case the field is omitted from the request body and the Meet server
     * applies its own secure default.
     * </p>
     */
    @Override
    public boolean createRoom( Map<String, Object> mapParameters )
    {
        String strRoomName = (String) mapParameters.get( PARAM_ROOM_NAME );

        MeetRoom room = _apiClient.createRoom( getBaseUrl( ), getClientId( ), getClientSecret( ), getScope( ),
                buildRoomRequest( (String) mapParameters.get( PARAM_ACCESS_LEVEL ), strRoomName ) );

        if ( room != null )
        {
            // The Meet API does not return a URL — build it from the slug
            if ( room.getUrl( ) == null && room.getSlug( ) != null )
            {
                room.setUrl( getMeetingUrlPattern( ).replace( PLACEHOLDER_SLUG, room.getSlug( ) ) );
            }

            _roomCache.put( strRoomName, room );
            AppLogService.info( "Meet provider — room '{}' created (Meet ID: {}, slug: {}, access level: {}, URL: {})", strRoomName, room.getId( ),
                    room.getSlug( ), room.getAccessLevel( ), room.getUrl( ) );
            return true;
        }

        AppLogService.error( "Meet provider — failed to create room '{}'", strRoomName );
        return false;
    }

    /**
     * Build the room creation body, resolving the access level from the per-call parameter then the site-wide default.
     *
     * <p>
     * An unrecognised value is logged and dropped rather than forwarded: the Meet API would reject the request, so falling back to the server default — itself
     * documented as a secure default — keeps room creation working.
     * </p>
     *
     * @param strRequestedAccessLevel
     *            the per-call access level, may be {@code null} or empty
     * @param strRoomName
     *            the room name, for logging only
     * @return the request body, never {@code null}
     */
    private MeetRoomRequest buildRoomRequest( String strRequestedAccessLevel, String strRoomName )
    {
        MeetRoomRequest request = new MeetRoomRequest( );

        String strAccessLevel = StringUtils.isNotEmpty( strRequestedAccessLevel ) ? strRequestedAccessLevel : getAccessLevel( );

        if ( StringUtils.isNotEmpty( strAccessLevel ) )
        {
            RoomAccessLevel accessLevel = RoomAccessLevel.fromValue( strAccessLevel );

            if ( accessLevel == null )
            {
                AppLogService.error( "Meet provider — unknown access level '{}' for room '{}', falling back to the Meet server default. Expected one of {}",
                        strAccessLevel, strRoomName, RoomAccessLevel.getValues( ) );
            }

            request.setAccessLevel( accessLevel );
        }

        return request;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Room deletion is not yet supported by the Meet API. This method removes the room from the local cache and returns {@code false}.
     * </p>
     */
    @Override
    public boolean deleteRoom( Map<String, Object> mapParameters )
    {
        String strRoomName = (String) mapParameters.get( PARAM_ROOM_NAME );

        AppLogService.info( "Meet provider — deleteRoom('{}') called but the Meet API does not yet support room deletion. " + "Removing from local cache only.",
                strRoomName );
        _roomCache.remove( strRoomName );
        return false;
    }

    // Token generation

    /**
     * {@inheritDoc}
     * <p>
     * The Meet API does not issue per-participant tokens. This method returns the room URL, which serves as the access point for all participants. Parameters
     * {@code identity}, {@code displayName}, and {@code notBefore} are ignored if present.
     * </p>
     */
    @Override
    public String generateParticipantToken( Map<String, Object> mapParameters )
    {
        String strRoomName = (String) mapParameters.get( PARAM_ROOM_NAME );
        MeetRoom room = _roomCache.get( strRoomName );

        if ( room == null )
        {
            AppLogService.error( "Meet provider — no room found in cache for name '{}'. Was createRoom() called first?", strRoomName );
            return null;
        }

        return room.getUrl( );
    }

    /**
     * {@inheritDoc}
     * <p>
     * The Meet API does not distinguish between participant and viewer access levels. This method delegates to
     * {@link #generateParticipantToken(Map)}.
     * </p>
     */
    @Override
    public String generateViewerToken( Map<String, Object> mapParameters )
    {
        return generateParticipantToken( mapParameters );
    }
}
