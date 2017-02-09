package vone;

public class WorkItemFactory {
	
		   //use getShape method to get object of type shape 
		   public Workitem getWorkitem(String workItemType){
		      if(workItemType == null){
		         return null;
		      }		
		      if(workItemType.equalsIgnoreCase("Defect")){
		         return new v1Defect();
		         
		      } else if(workItemType.equalsIgnoreCase("Epic")){
		         return new v1Epic();
		         
		      } else if(workItemType.equalsIgnoreCase("Story")){
		         return new v1Story();
		      }
		      
		      return null;
		   }
		}


