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
 * DTO for the OAuth2 token response from the Meet API.
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class MeetTokenResponse
{
    @JsonProperty( "access_token" )
    private String _strAccessToken;

    @JsonProperty( "token_type" )
    private String _strTokenType;

    @JsonProperty( "expires_in" )
    private long _lExpiresIn;

    @JsonProperty( "scope" )
    private String _strScope;

    public String getAccessToken( )
    {
        return _strAccessToken;
    }

    public void setAccessToken( String strAccessToken )
    {
        _strAccessToken = strAccessToken;
    }

    public String getTokenType( )
    {
        return _strTokenType;
    }

    public void setTokenType( String strTokenType )
    {
        _strTokenType = strTokenType;
    }

    public long getExpiresIn( )
    {
        return _lExpiresIn;
    }

    public void setExpiresIn( long lExpiresIn )
    {
        _lExpiresIn = lExpiresIn;
    }

    public String getScope( )
    {
        return _strScope;
    }

    public void setScope( String strScope )
    {
        _strScope = strScope;
    }
}
