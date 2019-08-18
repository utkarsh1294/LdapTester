package com.us.ldap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Scanner;

import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.Context;
import javax.naming.NamingEnumeration;

public class LdapTester {
	
	static Scanner sc= new Scanner(System.in);
    static SearchControls ctls = new SearchControls();
    static private InitialLdapContext context = null;
    static int PAGE_SIZE=0;
    static InputStreamReader streamin= new InputStreamReader(System.in);
    static BufferedReader bin= new BufferedReader(streamin);
    static String ldapConnectivityHost;
    static int port ;
    static String principal;
    static String credential;
    static LDAPNamespace namespace;
    static String inputFile;
    
    
	public static void main(String[] s) throws IOException, InterruptedException
	{
		int loop_times = 1;
		int loop_intervals = 0;
		
        namespace = new LDAPNamespace();
        
		if(s.length>=2)
		{
			loop_times=Integer.parseInt(s[0]);
			loop_intervals=Integer.parseInt(s[1]);
			if(s.length==3)
			{
				inputFile=s[2];
			}
		}
		else if(s.length==1)
		{
			inputFile=s[0];
			if(inputFile.equals("help"))
			{
				System.out.println("To use the utility you run in either of the below ways :"
						+ "\n1. java -jar LDAPTester.jar"
						+ "\n2. java -jar LDAPTester.jar <number of times to run> <sleep delay>"
						+ "\n3. java -jar LDAPTester.jar <number of times to run> <sleep delay> <Input File Name>"
						+ "\n4. java -jar LDAPTester.jar <Input File name>\n\n");
				
				System.exit(1);
			}
		}
		
        doSearch(loop_times,loop_intervals);
        
	}
	
	static boolean takeInput() throws IOException {
		
		File f = new File("Input.properties");
		
		if( inputFile!=null && !inputFile.isEmpty() )
		{
			f = new File(inputFile);
		}
		
		if(f.exists())
		{
			System.out.println("Found a file named Input.properties");
			
			FileInputStream fin= new FileInputStream(f.getAbsoluteFile());
			
			InputStreamReader finReader = new InputStreamReader(fin);
			
			bin = new BufferedReader(finReader);
			
			System.out.println("Reading Host Name");
			String readString= bin.readLine();
			ldapConnectivityHost = readString.substring(readString.indexOf('#')+1, readString.length());
	        
			System.out.println("Reading Port Number");
			readString= bin.readLine();
			port=Integer.parseInt(readString.substring(readString.indexOf('#')+1, readString.length()));
			
			System.out.println("Reading the principal name");
			readString= bin.readLine();
			principal = readString.substring(readString.indexOf('#')+1, readString.length());
		    
			if(!principal.isEmpty())
			{
				System.out.println("Reading the Password");
				readString= bin.readLine();
				credential = readString.substring(readString.indexOf('#')+1, readString.length());
			}
			else
				{
					System.out.println("Skipping the password Line");
					readString= bin.readLine();
				}
			
	        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	        
	         try {       	
	        	 context = init();
			} catch (Exception e) {
				System.out.print("Context Creation Failed");
				e.printStackTrace();
			}
	        
	        namespace = new LDAPNamespace();
	        System.out.println("Reading LDAP-Namespace identifier");
	        readString= bin.readLine();
	        namespace.setName(readString.substring(readString.indexOf('#')+1, readString.length()));
	        
	        System.out.println("Reading User SearchBase");
	        readString= bin.readLine();
	        namespace.setUserSearchBase(readString.substring(readString.indexOf('#')+1, readString.length()));
	        
	        System.out.println("Reading User Search Filter");
	        readString= bin.readLine();
	        namespace.setUserFilter(readString.substring(readString.indexOf('#')+1, readString.length()));
	        
	        System.out.println("Reading Group SearchBase");
	        readString= bin.readLine();
	        namespace.setGroupSearchBase(readString.substring(readString.indexOf('#')+1, readString.length()));
	        
	        System.out.println("Reading Group Search Filter");
	        readString= bin.readLine();
	        namespace.setGroupFilter(readString.substring(readString.indexOf('#')+1, readString.length()));
	        
	        System.out.println("Reading Pages Size");
	        readString= bin.readLine();
	        if(!readString.substring(readString.indexOf('#')+1, readString.length()).isEmpty())
	        	PAGE_SIZE=Integer.parseInt(readString.substring(readString.indexOf('#')+1, readString.length()));
	        
		}
		else
		{
		
			System.out.println("Enter Host Name");
			ldapConnectivityHost = bin.readLine();
	        
			System.out.println("Enter Port Number");
			port=sc.nextInt();
			
			System.out.println("Enter the principal name (for anonymous auth, leave blank)");
			principal = bin.readLine();
		    
			if(!principal.isEmpty())
			{
				System.out.println("Enter Password");
				credential = bin.readLine();
			}
			
			credential = bin.readLine();
			
	        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	        
	         try {       	
	        	 context = init();
			} catch (Exception e) {
				System.out.print("Context Creation Failed");
				e.printStackTrace();
			}
	        
	        System.out.println("Enter LDAP-Namespace identifier");
	        namespace.setName(bin.readLine());
	        
	        System.out.println("Enter User SearchBase");
	        namespace.setUserSearchBase(bin.readLine());
	        
	        System.out.println("Enter User Search Filter");
	        namespace.setUserFilter(bin.readLine());
	        
	        System.out.println("Enter Group SearchBase");
	        namespace.setGroupSearchBase(bin.readLine());
	        
	        System.out.println("Enter Group Search Filter");
	        namespace.setGroupFilter(bin.readLine());
	        
	        System.out.println("Enter Page Size (For turning paginantion off enter 0)");
	        PAGE_SIZE=Integer.parseInt(bin.readLine());
	        
		}	
        return true;
	}
	
	static void doSearch(int loop_times,int loop_intervals) throws IOException, InterruptedException
	{
		int loopCounter=0;
		
		boolean init=false;
		
		try {
			init = takeInput();
		}catch(IOException e)
		{
			System.out.println(e);
			e.printStackTrace();
		}
		
		if(init==false)
		{
			System.out.println("Input Failed");
			System.exit(1);
		}
		

		boolean disable_ldap_paging=(PAGE_SIZE==0?true:false);
		
		System.out.println("SEARCHING");
		
		NamingEnumeration<SearchResult> users_search_result=null;
        NamingEnumeration<SearchResult> groups_search_result=null;
        
        
        
        while(loopCounter<loop_times)
        	{
	            if (! disable_ldap_paging) {
	                try {
	    				context.setRequestControls(new Control[] { new PagedResultsControl(PAGE_SIZE,
	    				    Control.CRITICAL) });
	    			} catch (NamingException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}
	            }
        	
	        if(!namespace.getUserSearchBase().isEmpty()) {
		        try {
		        		users_search_result = context.search(namespace.getUserSearchBase(), namespace.getUserFilter(), ctls);
		        		
		        		if(users_search_result.hasMore())
		        			System.out.println("USER SEARCH RESULTS "+loopCounter+" : ");
		        		
			        		while(users_search_result.hasMore())
							{
								System.out.println(users_search_result.nextElement()+"\n");
							}	
			        		
		        		//System.out.print(users_search_result.nextElement()); --TRYING TO ENABLE PAGINATION
		        		
				} catch (NamingException e) {
					System.out.println("LDAP USER search failed");
					e.printStackTrace();
				}
	        }
	        
	        if(!namespace.getGroupSearchBase().isEmpty())
	        {
		        try {
						groups_search_result = context.search(namespace.getGroupSearchBase(), namespace.getGroupFilter(), ctls);
						if(groups_search_result.hasMore())
							System.out.println("GROUP SEARCH RESULTS "+loopCounter+" : ");
						
						while(groups_search_result.hasMore())
						{
							System.out.println(groups_search_result.nextElement());
						}
				} catch (NamingException e) {
					System.out.println("LDAP GROUP search failed");
					e.printStackTrace();
				}
	        }
	        Thread.sleep(loop_intervals*1000);
	        loopCounter++;
		}
	}
	
	static InitialLdapContext init() throws IOException
	{
		final Hashtable<String,Object> env = new Hashtable<String, Object>();
        
		
		InetAddress address= null;
		try {
			address = InetAddress.getByName(ldapConnectivityHost);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        String host = address.getCanonicalHostName();
        
        //Common LDAP Context params
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.BATCHSIZE, Integer.toString(PAGE_SIZE));
        env.put(Context.REFERRAL, "follow");
        env.put("java.naming.ldap.version", "3");
		env.put(Context.PROVIDER_URL, String.format("ldap://%s:%d", host, port));
		
		if (principal.isEmpty()) {
            // Anonymous authentication, don't set Principal and Credentials on environment.
        } else {
        	
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, principal);
            env.put(Context.SECURITY_CREDENTIALS, credential);
        }
		
			System.out.print(env);
		
		try {
			return new InitialLdapContext(env, null);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
			return null;
	}	
}