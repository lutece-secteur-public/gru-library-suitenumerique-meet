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
package fr.paris.lutece.plugins.meet.business;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for a room returned by the Meet API.
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class MeetRoom
{
    @JsonProperty( "id" )
    private String _strId;

    @JsonProperty( "slug" )
    private String _strSlug;

    @JsonProperty( "access_level" )
    private String _strAccessLevel;

    @JsonProperty( "url" )
    private String _strUrl;

    @JsonProperty( "telephony" )
    private Telephony _telephony;

    public String getId( )
    {
        return _strId;
    }

    public void setId( String strId )
    {
        _strId = strId;
    }

    public String getSlug( )
    {
        return _strSlug;
    }

    public void setSlug( String strSlug )
    {
        _strSlug = strSlug;
    }

    public String getAccessLevel( )
    {
        return _strAccessLevel;
    }

    public void setAccessLevel( String strAccessLevel )
    {
        _strAccessLevel = strAccessLevel;
    }

    public String getUrl( )
    {
        return _strUrl;
    }

    public void setUrl( String strUrl )
    {
        _strUrl = strUrl;
    }

    public Telephony getTelephony( )
    {
        return _telephony;
    }

    public void setTelephony( Telephony telephony )
    {
        _telephony = telephony;
    }

    /**
     * Nested DTO for telephony dial-in information.
     */
    @JsonIgnoreProperties( ignoreUnknown = true )
    public static class Telephony
    {
        @JsonProperty( "enabled" )
        private boolean _bEnabled;

        @JsonProperty( "pin_code" )
        private String _strPinCode;

        @JsonProperty( "phone_number" )
        private String _strPhoneNumber;

        @JsonProperty( "default_country" )
        private String _strDefaultCountry;

        public boolean isEnabled( )
        {
            return _bEnabled;
        }

        public void setEnabled( boolean bEnabled )
        {
            _bEnabled = bEnabled;
        }

        public String getPinCode( )
        {
            return _strPinCode;
        }

        public void setPinCode( String strPinCode )
        {
            _strPinCode = strPinCode;
        }

        public String getPhoneNumber( )
        {
            return _strPhoneNumber;
        }

        public void setPhoneNumber( String strPhoneNumber )
        {
            _strPhoneNumber = strPhoneNumber;
        }

        public String getDefaultCountry( )
        {
            return _strDefaultCountry;
        }

        public void setDefaultCountry( String strDefaultCountry )
        {
            _strDefaultCountry = strDefaultCountry;
        }
    }
}
