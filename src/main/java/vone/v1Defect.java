package vone;

import java.util.HashSet;

public class v1Defect extends Workitem implements IWorkItem {
	
	private String Resolution;
	private String FixedInBuild;
	private String Epic;
	private String FoundBy;
	private String ITSMReference;
	private String Estimate;
	private String StoryType;
	private HashSet PriorityQ;
	private HashSet PriorityQBacklog ;
	public String getResolution() {
		return   Resolution ;
	}
	public void setResolution(String resolution) {
		Resolution = resolution;
	}
	public String getFixedInBuild() {
		return FixedInBuild;
	}
	public void setFixedInBuild(String fixedInBuild) {
		FixedInBuild = fixedInBuild;
	}
	public String getEpic() {
		return Epic;
	}
	public void setEpic(String epic) {
		Epic = epic;
	}
	public String getFoundBy() {
		return FoundBy;
	}
	public void setFoundBy(String foundBy) {
		FoundBy = foundBy;
	}
	public HashSet<String> getPriorityQ() {
		return PriorityQ;
	}
	public HashSet<String> getPriorityQBacklog() {
		return PriorityQBacklog;
	}
	public void addPriorityQ(String priorityQ) {
		if (this.PriorityQ == null){
			PriorityQ = new HashSet() {
			};		
		}
		PriorityQ.add(priorityQ);
	}
	public void addPriorityQBackLogGroup(String backlogGroup) {
		if (this.PriorityQBacklog == null){
			PriorityQBacklog = new HashSet();		
		}
		PriorityQBacklog.add(backlogGroup);
	}
	public String getITSMReference() {
		return ITSMReference;
	}
	public void setITSMReference(String iTSMReference) {
		ITSMReference = iTSMReference;
	}
	public String getEstimate() {
		return Estimate;
	}
	public void setEstimate(String estimate) {
		Estimate = estimate;
	}
	public String getStoryType() {
		return StoryType;
	}
	public void setStoryType(String storyType) {
		StoryType = storyType;
	}






}
