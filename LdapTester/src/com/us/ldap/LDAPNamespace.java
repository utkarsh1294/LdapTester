package com.us.ldap;

public class LDAPNamespace {

	protected String name;
    protected String userSearchBase;
    protected String userFilter;
    protected String groupSearchBase;
    protected String groupFilter;
	
	
    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUserSearchBase() {
		return userSearchBase;
	}
	public void setUserSearchBase(String userSearchBase) {
		this.userSearchBase = userSearchBase;
	}
	public String getUserFilter() {
		return userFilter;
	}
	public void setUserFilter(String userFilter) {
		this.userFilter = userFilter;
	}
	public String getGroupSearchBase() {
		return groupSearchBase;
	}
	public void setGroupSearchBase(String groupSearchBase) {
		this.groupSearchBase = groupSearchBase;
	}
	public String getGroupFilter() {
		return groupFilter;
	}
	public void setGroupFilter(String groupFilter) {
		this.groupFilter = groupFilter;
	}

}
