/**
 * Copyright (C) 2011 Tom Narock and Eric Rozell
 *
 *     This software is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package essi.lod.abstracts;

import java.util.HashMap;
import java.util.Vector;


import essi.lod.entity.agu.Author;
import essi.lod.entity.agu.Keyword;
import essi.lod.entity.agu.Meeting;
import essi.lod.entity.agu.Section;
import essi.lod.entity.agu.Session;
import essi.lod.enumeration.AbstractEnumeration;
import essi.lod.exception.AbstractParserException;
import essi.lod.util.Utils;

/**
 * Extension of Abstract for handling HTML for AGU abstracts
 * @author Eric Rozell
 */
public class HtmlAbstract extends Abstract 
{
	private String _rawHtml;
	private Vector<Author> _authors;
	private Vector<Keyword> _keywords;
	private Meeting _meeting;
	private Section _section;
	private Session _session;
	private String _meetingId;
	private String _sectionId;
	private String _sessionId;
	private String _abstract;
	private String _title;
	private AbstractEnumeration _type;
	private String _abstractId;
	private String _hour;

	/**
	 * @constructor 
	 * @param text raw HTML for the abstract
	 * @throws AbstractParserException 
	 */
	public HtmlAbstract(String text) throws AbstractParserException
	{
		_rawHtml = text;
		_authors = new Vector<Author>();
		_keywords = new Vector<Keyword>();
		parseHtml();
		_meeting = new Meeting(_meetingId);
		_section = new Section(_sectionId, _meeting);
		_session = new Session(_sessionId.split("-")[0], _section);
	}
	
	private void parseHtml() throws AbstractParserException
	{
		int index, endIndex, nextIndex, emIndex, emEndIndex, afIndex, afEndIndex;
		
		// Hour (time of presentation)
		index = _rawHtml.indexOf("<span class=\"hr\">");
		endIndex = _rawHtml.indexOf("<br>", index);
		if (index < 0 || endIndex < 0)
		{
			//throw new AbstractParserException();
		}
		_hour = _rawHtml.substring(index+17, endIndex).trim();
		
		// Session
		index = _rawHtml.indexOf("<span class=\"an\">");
		endIndex = _rawHtml.indexOf("<br>", index);
		if (index < 0 || endIndex < 0)
		{
			throw new AbstractParserException();
		}
		String[] parts = _rawHtml.substring(index+17, endIndex).trim().split(" ");
		_abstractId = parts[0];
		_sessionId = _abstractId.split("-")[0];
		_type = Utils.getAbstractType((parts.length > 1) ? parts[1] : null);
		
		// Title
		index = _rawHtml.indexOf("<span class=\"ti\">");
		endIndex = _rawHtml.indexOf("<br>", index);
		if (index < 0 || endIndex < 0)
		{
			throw new AbstractParserException();
		}
		_title = _rawHtml.substring(index+17, endIndex).trim();


		// Authors
		index = _rawHtml.indexOf("<span class=\"au\">");
		if (index < 0)
		{
			throw new AbstractParserException();
		}
		HashMap<String,Author> identifier = new HashMap<String,Author>();
		while (index >= 0) 
		{
			Author a = null;
			endIndex = _rawHtml.indexOf("<br>", index);
			nextIndex = _rawHtml.indexOf("<span class=\"au\">", endIndex);
			
			if (endIndex >= 0)
			{
				String name = _rawHtml.substring(index+17, endIndex).trim();
				boolean add = false;
			
				if (identifier.containsKey(name))
				{
					a = identifier.get(name);
				}
				else
				{
					a = new Author(name);
					identifier.put(name,a);
					add = true;
				}
		  
				// Author email
				emIndex = _rawHtml.indexOf("<span class=\"em\">", endIndex);
				emEndIndex = _rawHtml.indexOf("<br>", emIndex);
				if ((emIndex < nextIndex || nextIndex == -1) && emIndex >= 0 && emEndIndex >= 0)
				{
					String email = _rawHtml.substring(emIndex+17, emEndIndex).trim();
					a.getPerson().addEmail(email);
				}
		  
				// Author affiliation
				afIndex = _rawHtml.indexOf("<span class=\"af\">", endIndex);
				afEndIndex = _rawHtml.indexOf("<br>", afIndex);
				if (afIndex < nextIndex && nextIndex >= 0 && afIndex >= 0 && afEndIndex >= 0)
				{
					String affiliation = _rawHtml.substring(afIndex+17, afEndIndex).trim();
					a.addAffiliation(affiliation);
				}

				if (add)
				{
					_authors.add(a);
				}
				
				index = nextIndex;
			}
		}
		// Abstract
		index = _rawHtml.indexOf("<span class=\"ab\">");
		endIndex = _rawHtml.indexOf("<br>", index);
		if (index < 0 || endIndex < 0)
		{
			throw new AbstractParserException();
		}
		_abstract = _rawHtml.substring(index+17, endIndex).trim();

		// Keywords
		index = _rawHtml.indexOf("<span class=\"de\">");
		while (index >= 0) 
		{
			endIndex = _rawHtml.indexOf("<br>", index);
			if (endIndex >= 0)
			{
				_keywords.add( new Keyword(_rawHtml.substring(index+17, endIndex).trim()) );
				index = _rawHtml.indexOf("<span class=\"de\">", endIndex);
			}
		}
		
		// Section
		index = _rawHtml.indexOf("<span class=\"sc\">");
		endIndex = _rawHtml.indexOf("<br>", index);
		if (index < 0 || endIndex < 0)
		{
			throw new AbstractParserException();
		}
		_sectionId = _rawHtml.substring(index+17, endIndex).trim();
		
		// AGU Meeting the abstract was submitted to
		index = _rawHtml.indexOf("<span class=\"mn\">");
		endIndex = _rawHtml.indexOf("<br>", index);
		if (index < 0 || endIndex < 0)
		{
			throw new AbstractParserException();
		}
		_meetingId = _rawHtml.substring(index+17, endIndex).trim();
	}

	@Override
	public String getTitle() 
	{
		return _title;
	}

	@Override
	public Meeting getMeeting() 
	{
		return _meeting;
	}

	@Override
	public String getId() 
	{
		return _abstractId;
	}

	@Override
	public Session getSession() 
	{
		return _session;
	}

	@Override
	public String getAbstract() 
	{
		return _abstract;
	}

	@Override
	public AbstractEnumeration getAbstractType() 
	{
		return _type;
	}

	@Override
	public Vector<Keyword> getKeywords() 
	{
		return _keywords;
	}

	@Override
	public Vector<Author> getAuthors() 
	{
		return _authors;
	}

	@Override
	public Section getSection() 
	{
		return _section;
	}

	@Override
	public String getHour() 
	{
		return _hour;
	}
}
