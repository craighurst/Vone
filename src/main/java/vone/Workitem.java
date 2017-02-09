package vone;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public  class Workitem implements IWorkItem {

	protected String Name;
	private String Project;
	private String Description;
	private String Reference;
	private String JiraLink;
	private String Source;
	private String Priority;
	private String Oid;
	private String Assignee;
	private Date CreationDate;
	private List<V1Comments> itemComments;
	private HashSet fixVersions;
	private HashSet Component;
	private HashSet ComponentBacklogGroup ;

	public void addComponent(String comp) {
		if (this.Component == null){
			Component = new HashSet() {
			};		
		}
		Component.add(comp);
	}
	public void addComponentBacklogGroup(String backlogGroup) {
		if (this.ComponentBacklogGroup == null){
			ComponentBacklogGroup = new HashSet();		
		}
		ComponentBacklogGroup.add(backlogGroup);
	}
	public HashSet<String> getComponents() {
		return Component;
	}
	public HashSet<String> getComponentBacklog() {
		return ComponentBacklogGroup;
	}

	public void addFixVersion(String fv) {
		if (this.fixVersions == null){
			fixVersions = new HashSet() {
			};		
		}
		fixVersions.add(fv);
	}
	public HashSet<String> getFixVersions() {
		return fixVersions;
	}
	
	
	@Override
	public String getName() {
		return Name;
	}

	@Override
	public void setName(String name) {
		Name = name;
	}

	public String getProject() {
		return Project;
	}

	public void setProject(String project) {
		Project = project;
	}

	public String getDescription() {
		return Description;
	}

	public void setDescription(String description) {
		Description = description;
	}

	public String getReference() {
		return Reference;
	}

	public void setReference(String reference) {
		Reference = reference;
	}

	public String getJiraLink() {
		return JiraLink;
	}

	public void setJiraLink(String jiraLink) {
		JiraLink = jiraLink;
	}

	public String getSource() {
		return Source;
	}

	public void setSource(String source) {
		Source = source;
	}

	public String getPriority() {
		return Priority;
	}

	public void setPriority(String priority) {
		Priority = priority;
	}

	public Date getCreationDate() {
		return CreationDate;
	}

	public void setCreationDate(Date creationDate) {
		CreationDate = creationDate;
	}

	public List<V1Comments> getItemComments() {
		return itemComments;
	}

	public void addItemComments(V1Comments itemComment) {
		if (this.itemComments == null){
			this.itemComments = new ArrayList<V1Comments>();
		}
		this.itemComments.add(itemComment) ;
	}


	public String getOid() {
		return Oid;
	}

	public void setOid(String oid) {
		Oid = oid;
	}

	public String getAssignee() {
		return Assignee;
	}
	public void setAssignee(String assignee) {
		Assignee = assignee;
	}
	@Override
	public String toString() {
		return String.format(
				"Workitem [Name=%s, Project=%s, Description=%s, Reference=%s, JiraLink=%s, Source=%s, Priority=%s, Oid=%s, Assignee=%s, CreationDate=%s, itemComments=%s, Component=%s, ComponentBacklogGroup=%s]",
				Name, Project, Description, Reference, JiraLink, Source, Priority, Oid, Assignee, CreationDate,
				itemComments, Component, ComponentBacklogGroup);
	}

}
