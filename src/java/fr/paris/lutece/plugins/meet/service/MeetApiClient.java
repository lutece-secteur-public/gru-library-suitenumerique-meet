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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.paris.lutece.plugins.meet.business.MeetRoom;
import fr.paris.lutece.plugins.meet.business.MeetRoomRequest;
import fr.paris.lutece.plugins.meet.business.MeetTokenResponse;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.httpaccess.HttpAccess;
import fr.paris.lutece.util.httpaccess.HttpAccessException;

/**
 * Low-level REST client for the Meet external API. Handles OAuth2 authentication and room operations using {@link HttpAccess}.
 */
public class MeetApiClient
{
    private static final String PATH_TOKEN = "/application/token/";
    private static final String PATH_ROOMS = "/rooms/";

    private static final String GRANT_TYPE = "client_credentials";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String EMPTY_JSON_BODY = "{}";

    private static final long TOKEN_EXPIRY_MARGIN_MS = 60_000L;

    private static final ObjectMapper _mapper = new ObjectMapper( ).configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

    private volatile String _strCachedToken;
    private volatile long _lTokenExpiresAt;

    /**
     * Obtain a valid access token, refreshing it if expired.
     *
     * @param strBaseUrl
     *            the Meet API base URL
     * @param strClientId
     *            the OAuth2 client ID
     * @param strClientSecret
     *            the OAuth2 client secret
     * @param strScope
     *            the user email to delegate
     * @return the access token, or {@code null} on failure
     */
    public String getAccessToken( String strBaseUrl, String strClientId, String strClientSecret, String strScope )
    {
        if ( _strCachedToken != null && System.currentTimeMillis( ) < _lTokenExpiresAt )
        {
            return _strCachedToken;
        }

        try
        {
            HttpAccess httpAccess = new HttpAccess( );

            Map<String, Object> tokenRequest = new HashMap<>( );
            tokenRequest.put( "client_id", strClientId );
            tokenRequest.put( "client_secret", strClientSecret );
            tokenRequest.put( "grant_type", GRANT_TYPE );
            tokenRequest.put( "scope", strScope );

            String strJsonBody = _mapper.writeValueAsString( tokenRequest );

            Map<String, String> headers = new HashMap<>( );
            headers.put( HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON );

            Map<String, String> responseHeaders = new HashMap<>( );

            String strResponse = httpAccess.doPostJSON( strBaseUrl + PATH_TOKEN, strJsonBody, headers, responseHeaders );

            MeetTokenResponse tokenResponse = _mapper.readValue( strResponse, MeetTokenResponse.class );

            _strCachedToken = tokenResponse.getAccessToken( );
            _lTokenExpiresAt = System.currentTimeMillis( ) + ( tokenResponse.getExpiresIn( ) * 1000 ) - TOKEN_EXPIRY_MARGIN_MS;

            return _strCachedToken;
        }
        catch( HttpAccessException e )
        {
            AppLogService.error( "Meet API — token request failed: {}", e.getMessage( ), e );
        }
        catch( Exception e )
        {
            AppLogService.error( "Meet API — token response parsing failed: {}", e.getMessage( ), e );
        }

        return null;
    }

    /**
     * Create a new room via the Meet API.
     *
     * @param strBaseUrl
     *            the Meet API base URL
     * @param strClientId
     *            the OAuth2 client ID
     * @param strClientSecret
     *            the OAuth2 client secret
     * @param strScope
     *            the user email to delegate
     * @return the created {@link MeetRoom}, or {@code null} on failure
     */
    public MeetRoom createRoom( String strBaseUrl, String strClientId, String strClientSecret, String strScope )
    {
        return createRoom( strBaseUrl, strClientId, strClientSecret, strScope, null );
    }

    /**
     * Create a new room via the Meet API, with an explicit request body.
     *
     * @param strBaseUrl
     *            the Meet API base URL
     * @param strClientId
     *            the OAuth2 client ID
     * @param strClientSecret
     *            the OAuth2 client secret
     * @param strScope
     *            the user email to delegate
     * @param request
     *            the room options; {@code null} or an unset request posts an empty body, letting the Meet server apply its defaults
     * @return the created {@link MeetRoom}, or {@code null} on failure
     */
    public MeetRoom createRoom( String strBaseUrl, String strClientId, String strClientSecret, String strScope, MeetRoomRequest request )
    {
        String strToken = getAccessToken( strBaseUrl, strClientId, strClientSecret, strScope );

        if ( strToken == null )
        {
            AppLogService.error( "Meet API — cannot create room: authentication failed" );
            return null;
        }

        try
        {
            HttpAccess httpAccess = new HttpAccess( );

            Map<String, String> headers = new HashMap<>( );
            headers.put( HEADER_AUTHORIZATION, BEARER_PREFIX + strToken );
            headers.put( HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON );

            Map<String, String> responseHeaders = new HashMap<>( );

            String strJsonBody = ( request == null ) ? EMPTY_JSON_BODY : _mapper.writeValueAsString( request );

            String strResponse = httpAccess.doPostJSON( strBaseUrl + PATH_ROOMS, strJsonBody, headers, responseHeaders );

            return _mapper.readValue( strResponse, MeetRoom.class );
        }
        catch( HttpAccessException e )
        {
            AppLogService.error( "Meet API — create room failed: {}", e.getMessage( ), e );
        }
        catch( Exception e )
        {
            AppLogService.error( "Meet API — create room response parsing failed: {}", e.getMessage( ), e );
        }

        return null;
    }

    /**
     * Retrieve a room by its ID.
     *
     * @param strBaseUrl
     *            the Meet API base URL
     * @param strClientId
     *            the OAuth2 client ID
     * @param strClientSecret
     *            the OAuth2 client secret
     * @param strScope
     *            the user email to delegate
     * @param strRoomId
     *            the room UUID
     * @return the {@link MeetRoom}, or {@code null} on failure
     */
    public MeetRoom getRoom( String strBaseUrl, String strClientId, String strClientSecret, String strScope, String strRoomId )
    {
        String strToken = getAccessToken( strBaseUrl, strClientId, strClientSecret, strScope );

        if ( strToken == null )
        {
            AppLogService.error( "Meet API — cannot retrieve room: authentication failed" );
            return null;
        }

        try
        {
            HttpAccess httpAccess = new HttpAccess( );

            Map<String, String> headers = new HashMap<>( );
            headers.put( HEADER_AUTHORIZATION, BEARER_PREFIX + strToken );

            String strResponse = httpAccess.doGet( strBaseUrl + PATH_ROOMS + "/" + strRoomId, null, null, headers );

            return _mapper.readValue( strResponse, MeetRoom.class );
        }
        catch( HttpAccessException e )
        {
            AppLogService.error( "Meet API — get room '{}' failed: {}", strRoomId, e.getMessage( ), e );
        }
        catch( Exception e )
        {
            AppLogService.error( "Meet API — get room response parsing failed: {}", e.getMessage( ), e );
        }

        return null;
    }
}
