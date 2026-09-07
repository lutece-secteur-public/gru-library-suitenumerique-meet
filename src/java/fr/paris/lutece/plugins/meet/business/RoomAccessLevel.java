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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Access levels accepted by the Meet API for the {@code access_level} field of a room.
 *
 * <p>
 * Mirrors the {@code RoomAccessLevel} schema of the Meet {@code openapi.yaml}. When the field is omitted from the creation request, the Meet server applies its
 * own secure default ({@link #TRUSTED}).
 * </p>
 */
public enum RoomAccessLevel
{
    /** Anyone with the room link can join directly, no authentication required. */
    PUBLIC( "public" ),

    /** Authenticated users join directly. Unauthenticated users wait in the lobby for approval. */
    TRUSTED( "trusted" ),

    /** Only participants explicitly trusted by the owner bypass the lobby. Everyone else waits for approval regardless of authentication. */
    RESTRICTED( "restricted" );

    private final String _strValue;

    RoomAccessLevel( String strValue )
    {
        _strValue = strValue;
    }

    /**
     * The wire value, as expected by the Meet API.
     *
     * @return the lowercase API value
     */
    @JsonValue
    public String getValue( )
    {
        return _strValue;
    }

    /**
     * Resolve an access level from its API value.
     *
     * @param strValue
     *            the API value, e.g. {@code "trusted"}
     * @return the matching level, or {@code null} if the value is {@code null} or unknown
     */
    public static RoomAccessLevel fromValue( String strValue )
    {
        for ( RoomAccessLevel level : values( ) )
        {
            if ( level._strValue.equals( strValue ) )
            {
                return level;
            }
        }

        return null;
    }

    /**
     * All API values, in declaration order.
     *
     * @return the list of supported access level values
     */
    public static List<String> getValues( )
    {
        List<String> listValues = new ArrayList<>( values( ).length );

        for ( RoomAccessLevel level : values( ) )
        {
            listValues.add( level._strValue );
        }

        return listValues;
    }
}
