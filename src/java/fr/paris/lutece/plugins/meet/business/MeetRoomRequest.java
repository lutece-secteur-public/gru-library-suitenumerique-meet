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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Body of a room creation request ({@code POST /rooms/}).
 *
 * <p>
 * Mirrors the {@code RoomCreate} schema of the Meet {@code openapi.yaml}: every field is optional, and the Meet server applies a secure default for each one
 * left out. Null fields are therefore excluded from the serialized body, so an unset request serializes to {@code {}} — the exact body posted before this DTO
 * existed.
 * </p>
 */
@JsonInclude( JsonInclude.Include.NON_NULL )
// Serialize the annotated fields only: the Lutece "_xxx" field naming does not match the getter name, so auto-detected accessors would
// otherwise be emitted as duplicate, unprefixed properties alongside the API ones.
@JsonAutoDetect( getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE )
public class MeetRoomRequest
{
    @JsonProperty( "access_level" )
    private RoomAccessLevel _accessLevel;

    /**
     * Returns the AccessLevel
     *
     * @return the access level, or {@code null} to let the Meet server apply its default
     */
    public RoomAccessLevel getAccessLevel( )
    {
        return _accessLevel;
    }

    /**
     * Sets the AccessLevel
     *
     * @param accessLevel
     *            the access level, or {@code null} to let the Meet server apply its default
     */
    public void setAccessLevel( RoomAccessLevel accessLevel )
    {
        _accessLevel = accessLevel;
    }
}
