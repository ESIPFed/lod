package org.esipfed.data;

import org.apache.log4j.Logger;
import java.io.File;
import java.util.HashMap;
import java.util.Vector;
import org.esipfed.owl.FOAF;
import org.esipfed.owl.TWC;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.agu.essi.util.FileWrite;
import org.agu.essi.util.Utils;
import org.esipfed.Person;

/**
 * Class to read ESIP Meeting Attendance 
 * data and write it out as FOAF RDF
 * @author Tom Narock
 */
public class MeetingAttendance {
	
	static final Logger log = Logger.getLogger(org.esipfed.data.MeetingAttendance.class);  
	
	/**
	 * Main Method 
	 * @param args (directory containing ESIP data, FOAF Person output filename, FOAF Organization output filename, FOAF Meeting
	 * output filename)
	 */
	public static void main ( String[] args ) {
	 
	  // input arguments 
      File dir = new File(args[0]);
	  String personFileName = args[1];
	  String orgFileName = args[2];
	  String meetFileName = args[3];
	  
	  FOAF foaf = new FOAF ();
	  TWC twc = new TWC ();
	  int index = 1;
	  FileWrite fw = new FileWrite();
	  Vector <Person> people = new Vector <Person> ();
	  
	  // write RDF/XML header information
	  StringBuilder strPeople = new StringBuilder();  
	  StringBuilder strOrg = new StringBuilder();    
	  StringBuilder strMeet = new StringBuilder();
      strPeople.append( Utils.writeXmlHeader() );
      strPeople.append( Utils.writeDocumentEntities() );
	  strPeople.append( Utils.writeRdfHeader() );
      strOrg.append( Utils.writeXmlHeader() );
      strOrg.append( Utils.writeDocumentEntities() );
	  strOrg.append( Utils.writeRdfHeader() );
	  strMeet.append( Utils.writeXmlHeader() );
      strMeet.append( Utils.writeDocumentEntities() );
	  strMeet.append( Utils.writeRdfHeader() );
	  
	  // directory containing ESIP CSV meeting attendee files
	  File[] files = dir.listFiles();
	  
	  // loop over all the files
	  String[] parts;
	  String firstName = "";
	  String lastName = "";
	  String emailAddress = "";
	  String affiliation = "";
	  String meeting = "";
	  boolean exists;
	  for (int i=0; i<files.length; i++) {
		  
		// read the file line by line
		try {
		    FileInputStream fstream = new FileInputStream( files[i].toString() );
		    DataInputStream in = new DataInputStream( fstream );
		    BufferedReader br = new BufferedReader( new InputStreamReader(in) );
		    String strLine;
		    int counter = 0; 
		    while ( (strLine = br.readLine()) != null )   {
		      if ( counter != 0 ) { // ignore the first line (header)
		        parts = strLine.split(",");
		        firstName = parts[2];
		        lastName = parts[3];
		        affiliation = parts[5];
		        affiliation = affiliation.replace("\"", ""); // some affiliations in original ESIP data have erroneous " values
		        emailAddress = parts[14];
		        meeting = parts[15];
		        
		        // create a new person object
		        Person p = new Person(firstName, lastName, affiliation, emailAddress, meeting);
		        
		        // create an id for this person
		        p.createID(index);
				index++;
		        
		        // does this person already exist?
		        //  if yes, then add the email address and affiliation to the existing person
		        exists = p.exists( people, affiliation, emailAddress, meeting );
		        if ( !exists ) { people.add(p); }
		        
		      }
		      counter++;
		    }
		    in.close();
		} catch (Exception e) { 
			log.error("Error reading ESIP Attendance: " + e.getMessage());
			log.error("File: " + files[i].toString());
			log.error(" ");
		}
			 		
	  } // end for
	  
	  // write Person RDF
	  for ( int i=0; i<people.size(); i++ ) {
	    Person person = people.get(i);
	    strPeople.append( foaf.writePerson(person) );
	  }
	  strPeople.append( Utils.writeRdfFooter() );	  
	  fw.newFile( personFileName, strPeople.toString() );
	  
	  // create/write Organizational RDF
	  HashMap <String, Vector <String>> orgs = foaf.createOrgMap(people);
	  strOrg.append( foaf.writeOrganization(orgs) );
	  strOrg.append( Utils.writeRdfFooter() );	  
	  fw.newFile( orgFileName, strOrg.toString() );
	  
	  // create/write Meeting RDF
	  HashMap <String, Vector <String>> meetings = twc.createMeetingMap(people);
	  strMeet.append( twc.writeMeetings(meetings) );
	  strMeet.append( Utils.writeRdfFooter() );
	  fw.newFile( meetFileName, strMeet.toString() );

	}
	
}