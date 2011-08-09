package com.chatfe;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.ContentResolver;
import android.app.Activity;
import android.util.Log;

public class TopicsXMLHandler extends DefaultHandler {
	
	private static final String TAG = UpdateService.class.getSimpleName();
	Boolean currentElement = false;
	String currentValue = null;
	public String topicText = null;
	public String topicImgUrl = null;
	
	
	/*public static TopicsList topicsList = null;
	
	public static TopicsList getTopicsList(){
		return topicsList;
	}
	
	public static void setTopicsList(TopicsList topicsList){
		TopicsXMLHandler.topicsList = topicsList;
	
	}*/
	
	@Override
	public void startElement(String uri, String localName, String qName,  Attributes attributes) throws SAXException{
				
				currentElement = true;
				
				if(localName.equals("topicText"))
				{
					//Log.e(TAG, "topicText b4: " + TopicsXMLHandler.topicText);					
				}
				else if(localName.equals("topicImgUrl")){
					
					//Log.e(TAG, "topicImgUrl b4: " + TopicsXMLHandler.topicImgUrl);	
				
				}
		
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException{
		currentElement = false;
		
		if(localName.equalsIgnoreCase("topicText")){
			
			topicText = currentValue;
			//Log.e(TAG, "in topicText: " + topicText);
		}
		else if(localName.equalsIgnoreCase("topicImgUrl")){
			topicImgUrl = currentValue;
			//Log.e(TAG, "in topicImgUrl: " + topicImgUrl);
		
		}
		
		
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException{
		if (currentElement) {
			//Log.e(TAG, "character length: " + length);
			currentValue = new String(ch, start, length);
			currentElement = false;
			}
	}
	
	
	
}
