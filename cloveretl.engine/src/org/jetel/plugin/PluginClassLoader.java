/*
 * jETeL/CloverETL - Java based ETL application framework.
 * Copyright (c) Javlin, a.s. (info@cloveretl.com)
 *  
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jetel.plugin;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jetel.util.CompoundEnumeration;
import org.jetel.util.classloader.GreedyURLClassLoader;

/**
 * Plugin class loader is descendant of URLClassLoader and is used for particular plugins.
 * Added value is in loading classes from siblings plugins.
 * Fistly try loading class from system, then from ourselfs and finally from its siblings.
 * 
 * @author Martin Zatopek
 *
 */
public class PluginClassLoader extends GreedyURLClassLoader {
	
	static final Logger log = Logger.getLogger(PluginClassLoader.class);

    private PluginDescriptor pluginDescriptor;
    private PluginDescriptor[] importedPlugins;
    
    /**
     * Constructor.
     * @param parent parent class loader
     * @param pluginDescriptor reference to a plugin tided with this class loader
     * @param greedy whether this class loader behaves greedy or not
     * @param excludedPackages package prefixes of classes which are excluded from given loading algorithm (greedy/regular)
     */
    public PluginClassLoader(ClassLoader parent, PluginDescriptor pluginDescriptor, boolean greedy, String[] excludedPackages) {
        super(pluginDescriptor.getLibraryURLs(), parent, excludedPackages, greedy);
        this.pluginDescriptor = pluginDescriptor;
        if (log.isTraceEnabled()) {
        	log.trace("Init " + this + " parent:" + parent + " greedy:" + greedy);
        	if (pluginDescriptor.getLibraryURLs() != null) {
        		log.trace("Init " + this + " libraries:" + Arrays.asList(pluginDescriptor.getLibraryURLs()));
        	}
        	if (excludedPackages != null) {
        		log.trace("Init " + this + " excludedPackages:" + Arrays.asList(excludedPackages) );
        	}
        }
        collectImports();
    }

    /**
     * Collect all plugin descriptors, on which depends this plugin. 
     */
    private void collectImports() {
        PluginDescriptor pluginDescriptor;
        Collection<PluginPrerequisite> prerequisites = getPluginDescriptor().getPrerequisites();
        List<PluginDescriptor> importPlugins = new ArrayList<PluginDescriptor>();
        
        for(Iterator<PluginPrerequisite> it = prerequisites.iterator(); it.hasNext();) {
            PluginPrerequisite prerequisite = it.next();
            pluginDescriptor = Plugins.getPluginDescriptor(prerequisite.getPluginId());
            if (pluginDescriptor != null) {
                importPlugins.add(pluginDescriptor);
            }
        }
        
        this.importedPlugins = importPlugins.toArray(new PluginDescriptor[importPlugins.size()]);
        if (log.isTraceEnabled()) {
        	log.trace(this + " importPlugins:" + importPlugins);
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return super.findClass(name);
        } catch (ClassNotFoundException e) {
            return findClassInPrerequisities(name);
        }
    }
    
   private Class<?> findClassInPrerequisities(String name) throws ClassNotFoundException {
       //try all dependant plugins
       for (PluginDescriptor pluginDescriptor : importedPlugins) {
           try {
               if (log.isTraceEnabled()) {
                   log.trace(this + " trying to find:" + name + " in " + getClassLoaderId(pluginDescriptor.getClassLoader()));
               }
               Class<?> cl = pluginDescriptor.getClassLoader().loadClass(name);
               if (log.isTraceEnabled() && cl != null) {
                   log.trace(this + " class:" + name + " found by " + getClassLoaderId(cl.getClassLoader()));
               }
               return cl;
           } catch(ClassNotFoundException e) {
               //continue;
           }
       }
       
       throw new ClassNotFoundException(name);
   }

	@Override
	public URL getResource(String name) {
		URL resource = super.getResource(name);
		if (resource == null) {
			for (PluginDescriptor pluginDescriptor : importedPlugins) {
				resource = pluginDescriptor.getClassLoader().getResource(name);
				if (resource != null) {
					return resource;
				}
			}
		}
		return resource;
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		List<Enumeration<URL>> resources = new ArrayList<Enumeration<URL>>();
		resources.add(super.getResources(name));
		for (PluginDescriptor pluginDescriptor : importedPlugins) {
			resources.add(pluginDescriptor.getClassLoader().getResources(name));
		}
		return new CompoundEnumeration<URL>(resources);
	}

    public PluginDescriptor getPluginDescriptor() {
        return pluginDescriptor;
    }
    
    /**
     * Answers URLs composing this class loader's class path. Class path of all imported
     * plugins is included in the result, the lookup is transitive.
     */
    @Override
    public URL[] getURLs() {
    	
    	Set<URL> urls = new LinkedHashSet<>();
    	
    	urls.addAll(Arrays.asList(super.getURLs()));
    	
    	for (PluginDescriptor pd : importedPlugins) {
    		if (pd.getClassLoader() instanceof PluginClassLoader) {
    			PluginClassLoader loader = (PluginClassLoader)pd.getClassLoader();
    			urls.addAll(Arrays.asList(loader.getURLs()));
    		}
    	}
    	
    	return urls.toArray(new URL[urls.size()]);
    }
    
    @Override
	public String toString(){
    	return "PluginClassLoader:" + this.getPluginDescriptor().getId();
    }
}