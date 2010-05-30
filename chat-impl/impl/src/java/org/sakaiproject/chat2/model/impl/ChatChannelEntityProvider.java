package org.sakaiproject.chat2.model.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.chat2.model.ChatChannel;
import org.sakaiproject.chat2.model.ChatManager;
import org.sakaiproject.courier.api.CourierService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;

public class ChatChannelEntityProvider implements CoreEntityProvider, AutoRegisterEntityProvider, 
	Outputable, Resolvable, Describeable, CollectionResolvable, ActionsExecutable {

	private ChatManager chatManager;
	  
	public final static String ENTITY_PREFIX = "chat-channel";

	protected final Log LOG = LogFactory.getLog(getClass());
	
	public class SimpleChatChannel {

		private String id;
		private String placement;
		private String context;
		private String title;
		private String description;
		private boolean defaultForContext;
		
		public SimpleChatChannel()
		{
		}
		
		public SimpleChatChannel(ChatChannel channel)
		{
			this.id = channel.getId();
			this.placement = channel.getPlacement();
			this.context = channel.getContext();
			this.title = channel.getTitle();
			this.description = channel.getDescription();
			this.defaultForContext = false;
		}
		
		public String getId() {
			return id;
		}
		public void setPlacement(String placement) {
			this.placement = placement;
		}
		public String getPlacement() {
			return placement;
		}
		public void setContext(String context) {
			this.context = context;
		}
		public String getContext() {
			return context;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getTitle() {
			return title;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public String getDescription() {
			return description;
		}

		public void setDefaultForContext(boolean defaultForContext) {
			this.defaultForContext = defaultForContext;
		}

		public boolean isDefaultForContext() {
			return defaultForContext;
		}

	}
	
	public boolean entityExists(String id) {

		if (id == null) {
			return false;
		}
		
		if ("".equals(id))
			return false;
		
		return (chatManager.getChatChannel(id) != null);
	}

	public Object getEntity(EntityReference ref) {

		String id = ref.getId();
		
		if (id == null || "".equals(id)) {
		         return new SimpleChatChannel();
		}
		  
		ChatChannel channel = chatManager.getChatChannel(id);

		if (channel == null) {
			throw new IllegalArgumentException("Channel not found");
		}
		
		if (!chatManager.getCanReadMessage(channel)) {
			throw new SecurityException("You do not have permission to access this chat channel");
		}
		
		return new SimpleChatChannel(channel);
	}

	public Object getSampleEntity() {
		return new SimpleChatChannel();
	}

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	public ChatManager getChatManager() {
		return chatManager;
	}
	
	public void setChatManager(ChatManager chatManager) {
		this.chatManager = chatManager;
	}

	public String[] getHandledOutputFormats() {
	    return new String[] { Formats.HTML, Formats.XML, Formats.JSON, Formats.FORM };
	}

	public List<SimpleChatChannel> getEntities(EntityReference ref, Search search) {

		List<SimpleChatChannel> channels = new ArrayList<SimpleChatChannel>();
		
		Restriction locRes = search.getRestrictionByProperty(CollectionResolvable.SEARCH_LOCATION_REFERENCE);
		
        if (locRes != null) {
        	String location = locRes.getStringValue();
        	String context = new EntityReference(location).getId();
            
        	ChatChannel defaultChannel = getChatManager().getDefaultChannel(context, null);
        	
			List<ChatChannel> contextChannels = getChatManager().getContextChannels(context, true);
		
			for (ChatChannel c : contextChannels) {
	        	if (getChatManager().getCanReadMessage(c)) {
	        		SimpleChatChannel sc = new SimpleChatChannel(c);
	        		if (defaultChannel != null && sc.getId().equals(defaultChannel.getId())) {
	        			sc.setDefaultForContext(true);
	        		}
	        		channels.add(sc);
	        	}
			}
        }
        
		return channels;
	}

	// Custom action to start or stop listening to a channel
	
	 @EntityCustomAction(action="listen",viewKey=EntityView.VIEW_EDIT)
	 public boolean listen(EntityReference ref, Map<String, Object> params) {

		String id = ref.getId();
		
		if (id == null || "".equals(id)) {
		         return false;
		}
		  
		ChatChannel channel = chatManager.getChatChannel(id);

		if (channel == null) {
			throw new IllegalArgumentException("Channel not found");
		}
		
		if (!chatManager.getCanReadMessage(channel)) {
			throw new SecurityException("You do not have permission to access this chat channel");
		}
		
	    Session session = SessionManager.getCurrentSession();
	    String sessionId = session.getId();
	      
	    CourierService courier = org.sakaiproject.courier.cover.CourierService.getInstance();
	    
		ChatRestListener listener = new ChatRestListener(chatManager, courier, sessionId, channel); 

		chatManager.addRoomListener(listener, channel.getId());
		
		return true;
	}
}
