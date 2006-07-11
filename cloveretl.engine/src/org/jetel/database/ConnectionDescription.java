/*
*    jETeL/Clover - Java based ETL application framework.
*    Copyright (C) 2002-04  David Pavlis <david_pavlis@hotmail.com>
*    
*    This library is free software; you can redistribute it and/or
*    modify it under the terms of the GNU Lesser General Public
*    License as published by the Free Software Foundation; either
*    version 2.1 of the License, or (at your option) any later version.
*    
*    This library is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU    
*    Lesser General Public License for more details.
*    
*    You should have received a copy of the GNU Lesser General Public
*    License along with this library; if not, write to the Free Software
*    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
*/
package org.jetel.database;

import org.jetel.data.GraphElementDescription;
import org.jetel.plugin.Extension;
import org.jetel.plugin.PluginDescriptor;

/**
 * @author Martin Zatopek
 *
 */
public class ConnectionDescription extends GraphElementDescription {

    public final static String EXTENSION_POINT_ID = "connection";
    
    public ConnectionDescription(String connectionType, String className, PluginDescriptor pluginDescriptor) {
        super(EXTENSION_POINT_ID, connectionType, className, pluginDescriptor);
    }

    public ConnectionDescription(Extension connectionExtension) {
        super(EXTENSION_POINT_ID, connectionExtension);
    }

}
