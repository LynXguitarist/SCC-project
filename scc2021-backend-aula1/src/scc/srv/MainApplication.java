package scc.srv;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import data.Entity;
import scc.rest.CalendarResource;
import scc.rest.ForumResource;
import scc.rest.MediaResource;

public class MainApplication extends Application {

	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> resources = new HashSet<Class<?>>();

	public MainApplication() {
		resources.add(ControlResource.class);
		resources.add(MediaResource.class);
		resources.add(Entity.class);
		resources.add(ForumResource.class);
		resources.add(CalendarResource.class);
	}

	@Override
	public Set<Class<?>> getClasses() {
		return resources;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}
