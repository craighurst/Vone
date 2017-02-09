package vone;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.JiraRestClientFactory;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.versionone.Oid;
import com.versionone.apiclient.Asset;
import com.versionone.apiclient.Query;
import com.versionone.apiclient.Services;
import com.versionone.apiclient.V1Connector;
import com.versionone.apiclient.exceptions.APIException;
import com.versionone.apiclient.exceptions.ConnectionException;
import com.versionone.apiclient.exceptions.OidException;
import com.versionone.apiclient.filters.FilterTerm;
import com.versionone.apiclient.interfaces.IAssetType;
import com.versionone.apiclient.interfaces.IAttributeDefinition;
import com.versionone.apiclient.interfaces.IServices;
import com.versionone.apiclient.services.QueryResult;

public class testJira2 {
	public static final String ScopeType = "Scope";
	public static final String FeatureGroupType = "Theme";
	public static final String StoryType = "Story";
	public static final String DefectType = "Defect";
	public static final String EpicType = "Epic";
	public static final String TaskType = "Task";
	public static final String TestType = "Test";
	public static final String ChangeSetType = "ChangeSet";
	public static final String BuildProjectType = "BuildProject";
	public static final String BuildRunType = "BuildRun";
	public static final String PrimaryWorkitemType = "PrimaryWorkitem";
	public static final String WorkitemType = "Workitem";
	public static final String MemberType = "Member";
	public static final String LinkType = "Link";
	public static final String AttributeDefinitionType = "AttributeDefinition";

	public static final String SystemAdminRoleName = "Role.Name'System Admin";
	public static final String SystemAdminRoleId = "Role:1";

	public static final String OwnersAttribute = "Owners";
	public static final String AssetStateAttribute = "AssetState";
	public static final String AssetTypeAttribute = "AssetType";

	public static final String DeleteOperation = "Delete";
	public static final String InactivateOperation = "Inactivate";
	public static final String ReactivateOperation = "Reactivate";

	public static final String WorkitemPriorityType = "WorkitemPriority";
	public static final String DefectPriorityType = "Defect.Priority";
	public static final String WorkitemSourceType = "StorySource";
	public static final String WorkitemStatusType = "StoryStatus";
	public static final String BuildRunStatusType = "BuildStatus";

	private static final String IdAttribute = "ID";
	private static final String AssetAttribute = "Asset";

	private static final String PriorityLow = "WorkitemPriority:138";
	private static final String PriorityMedium = "WorkitemPriority:139";
	private static final String PriorityHigh = "WorkitemPriority:140";
	private static final String Source = "StorySource:5168";

	private static final String JIRAIssueUrlTemplate = "Services.JiraService.JIRAIssueUrlTemplate";

	private static final String JIRA_URL = "Services.JiraService.JIRAUrl";
	private static final String JIRA_ADMIN_USERNAME = "Services.JiraService.JIRAUserName";
	private static final String JIRA_ADMIN_PASSWORD = "Services.JiraService.JIRAPassword";
	private static final String JiraDefectFilter = "Services.JiraService.DefectFilter";

	private static final String VersionOneURL = "Services.VersionOneService.Settings.V1Url";
	private static final String VersionOneAccessToken = "Services.VersionOneService.Settings.AccessToken";
	private static final String versionOneProject = "Services.JiraService.ProjectMappings.Mapping.VersionOneProject";
	private static final String JIRAIssueUrlTitle = "Services.JiraService.JIRAIssueUrlTitle";

	public static void main(String[] args) throws Exception {

		Configuration cfg = readproperties();

		V1AssetProcessor myv1Asset = new V1AssetProcessor(cfg.getString(VersionOneURL),cfg.getString(VersionOneAccessToken));
		
		
		System.out.println(String.format("Logging in to %s with username '%s' ", cfg.getString(JIRA_URL),
				cfg.getString(JIRA_ADMIN_USERNAME)));

		JiraRestClientFactory jiraClientFactory = new AsynchronousJiraRestClientFactory();

		URI uri = new URI(cfg.getString(JIRA_URL));

		JiraRestClient jiraClient = jiraClientFactory.createWithBasicHttpAuthentication(uri,
				cfg.getString(JIRA_ADMIN_USERNAME), cfg.getString(JIRA_ADMIN_PASSWORD));

		// Promise<Issue> promiseIssue =
		// jiraClient.getIssueClient().getIssue("TEST-1");
		Issue issue = null;
		Issue jiraEpic = null;
		String description = null;

//		V1Connector versionOneConnector = V1Connector.withInstanceUrl(cfg.getString(VersionOneURL))
//				.withUserAgentHeader("AppName", "1.0").withAccessToken(cfg.getString(VersionOneAccessToken)).build();
//
//		IServices versionOneServices = new Services(versionOneConnector);

		SearchResult jiraSearchResult = jiraClient.getSearchClient()
				.searchJql("filter=" + cfg.getString(JiraDefectFilter)).claim();

		for (BasicIssue is : jiraSearchResult.getIssues()) {

			issue = jiraClient.getIssueClient().getIssue(is.getKey()).claim();

			IAttributeDefinition nameAttribute;
			IAttributeDefinition priorityAttribute;

//			Oid projectId = versionOneServices.getOid(cfg.getString(versionOneProject));
			
			v1Defect defect = new v1Defect();
			
			
			defect.setProject(cfg.getString(versionOneProject));
			
			defect.setName(issue.getSummary());
			
			
			
//			IAssetType storyType = versionOneServices.getMeta().getAssetType(DefectType);
//
//			Asset newStory = versionOneServices.createNew(storyType, projectId);

//			nameAttribute = storyType.getAttributeDefinition("Name");
//			priorityAttribute = storyType.getAttributeDefinition("Priority");

//			newStory.setAttributeValue(nameAttribute, issue.getSummary());
//			System.out.println("description found.........." + issue.getDescription());
//			System.out.println("Epic Link.........." + issue.getFieldByName("Epic Link").getValue());

			// try{
			// Iterable<Field> fieldList = issue.getFields();
			//
			// for (Field f : fieldList)
			// {
			// System.out.println(f.getName() + " "+ f.getValue());
			//
			// if (f.getName().equals("Description") ){
			// System.out.println("description found..........");
			// }
			// }
			// description = issue.getFieldByName("Technical
			// Description").getValue().toString();
			// } catch (Exception e) {
			// // TODO Auto-generated catch block
			// description =
			// issue.getFieldByName("Description").getValue().toString();
			//
			// }

			Iterable<Version> versionList = issue.getFixVersions();
			String lastFixVersion = null;
			for (Version f : versionList) {
				System.out.println(f.getName() + " " + f.getDescription());
				lastFixVersion = f.getDescription();
				if (f.getName().equals("Description")) {
					System.out.println("description found..........");
				}
			}
			if (issue.getFieldByName("Technical Description").getValue() == null) {
				System.out.println("No technical description");
				defect.setDescription("No Description");
			} else {
			defect.setDescription(txtToHtml(issue.getFieldByName("Technical Description").getValue().toString()));
//				newStory.setAttributeValue(storyType.getAttributeDefinition("Description"),
//						txtToHtml(issue.getFieldByName("Technical Description").getValue().toString()));

			}

defect.setResolution(txtToHtml(issue.getDescription()));
defect.setFixedInBuild(lastFixVersion);
defect.setReference(issue.getKey());
//			newStory.setAttributeValue(storyType.getAttributeDefinition("Resolution"),
//					txtToHtml(issue.getDescription()));
//			newStory.setAttributeValue(storyType.getAttributeDefinition("FixedInBuild"), lastFixVersion);
//			newStory.setAttributeValue(storyType.getAttributeDefinition("Reference"), issue.getKey());

			String VersionOnePriority = null;
			String JiraPriority = null;

			@SuppressWarnings("unchecked")
			List<HierarchicalConfiguration<ImmutableNode>> fields = ((HierarchicalConfiguration<ImmutableNode>) cfg)
					.configurationsAt("Services.JiraService.PriorityMappings.Mapping");
			for (@SuppressWarnings("rawtypes")
			HierarchicalConfiguration sub : fields) {
				JiraPriority = sub.getString("JIRAPriority");
				if (issue.getPriority().getName().equals(JiraPriority)) {
					VersionOnePriority = sub.getString("VersionOnePriority");
					break;
				}
			}
			
			
			defect.setPriority(VersionOnePriority);
			defect.setSource(Source);
			defect.setFoundBy(issue.getReporter().getDisplayName());
			defect.setJiraLink(cfg.getString(JIRAIssueUrlTemplate) + issue.getKey());
			
			
			myv1Asset.createDefect(defect);
			
//			newStory.setAttributeValue(priorityAttribute, VersionOnePriority);
//			newStory.setAttributeValue(storyType.getAttributeDefinition("Source"), Source);
//
//			newStory.setAttributeValue(storyType.getAttributeDefinition("FoundBy"),
//					issue.getReporter().getDisplayName());
//
//			versionOneServices.save(newStory);
//			addJiraLink(cfg, issue, versionOneServices, newStory);
//
//			System.out.println(newStory.getOid().getToken());
//			System.out.println(newStory.getAttribute(storyType.getAttributeDefinition("Scope")).getValue());
//			System.out.println(newStory.getAttribute(nameAttribute).getValue());

//			IAssetType epicType = versionOneServices.getMeta().getAssetType(EpicType);
//
//			Asset newEpic = versionOneServices.createNew(epicType, projectId);
//			nameAttribute = epicType.getAttributeDefinition("Name");
//			priorityAttribute = epicType.getAttributeDefinition("Priority");
//			String jiraEpicName = issue.getFieldByName("Epic Link").getValue().toString();
//
//			jiraEpic = jiraClient.getIssueClient().getIssue(jiraEpicName).claim();
//
//			Query query = new Query(epicType);
//			IAttributeDefinition referenceAttribute = epicType.getAttributeDefinition("Reference");
//			IAttributeDefinition epicNumberAttribute = epicType.getAttributeDefinition("Number");
//			query.getSelection().add(referenceAttribute);
//			query.getSelection().add(nameAttribute);
//			query.getSelection().add(epicNumberAttribute);
//			FilterTerm referenceTerm = new FilterTerm(referenceAttribute);
//			referenceTerm.equal(jiraEpic.getKey());
//			query.setFilter(referenceTerm);
//			QueryResult result = versionOneServices.retrieve(query);
//			if (result.getTotalAvaliable() > 0) {
//
//				for (Asset resultEpic : result.getAssets()) {
//
//					newStory.setAttributeValue(storyType.getAttributeDefinition("Super"), resultEpic.getOid());
//					versionOneServices.save(newStory);
//				}
//			} else {
//
//				newEpic.setAttributeValue(nameAttribute, jiraEpic.getSummary());
//				System.out.println("description found.........." + jiraEpic.getDescription());
//				newEpic.setAttributeValue(storyType.getAttributeDefinition("Description"),
//						txtToHtml(issue.getDescription()));
//				// newEpic.setAttributeValue(storyType.getAttributeDefinition("FixedInBuild"),
//				// lastFixVersion);
//				newEpic.setAttributeValue(storyType.getAttributeDefinition("Reference"), jiraEpic.getKey());
//
//				@SuppressWarnings("unchecked")
//				List<HierarchicalConfiguration<ImmutableNode>> fields2 = ((HierarchicalConfiguration<ImmutableNode>) cfg)
//						.configurationsAt("Services.JiraService.EpicPriorityMappings.Mapping");
//				for (@SuppressWarnings("rawtypes")
//				HierarchicalConfiguration sub : fields2) {
//					JiraPriority = sub.getString("JIRAPriority");
//					if (issue.getPriority().getName().equals(JiraPriority)) {
//						VersionOnePriority = sub.getString("VersionOnePriority");
//						break;
//					}
//				}
//				newEpic.setAttributeValue(priorityAttribute, VersionOnePriority);
//
//				versionOneServices.save(newEpic);
//				addJiraLink(cfg, jiraEpic, versionOneServices, newEpic);
//
//				newStory.setAttributeValue(storyType.getAttributeDefinition("Super"), newEpic.getOid());
//				versionOneServices.save(newStory);
//
//			}
		}
		// Print the result
		System.out.println(issue.getAssignee() + "   " + issue.getSummary() + "  " + issue.getWorklogs());
		System.exit(0);
	}

	private static boolean noEpic(Issue jiraEpic, IServices versionOneServices, IAttributeDefinition nameAttribute,
			IAssetType epicType) throws ConnectionException, APIException, OidException {
		Query query = new Query(epicType);
		IAttributeDefinition referenceAttribute = epicType.getAttributeDefinition("Reference");
		IAttributeDefinition epicNumberAttribute = epicType.getAttributeDefinition("Number");
		query.getSelection().add(referenceAttribute);
		query.getSelection().add(nameAttribute);
		query.getSelection().add(epicNumberAttribute);

		FilterTerm referenceTerm = new FilterTerm(referenceAttribute);
		referenceTerm.equal(jiraEpic.getKey());
		query.setFilter(referenceTerm);
		QueryResult result = versionOneServices.retrieve(query);
		if (result.getTotalAvaliable() > 0) {
			return false;
		}

		for (Asset task : result.getAssets()) {
			System.out.println(task.getOid().getToken());
			System.out.println(task.getOid().getKey());
			System.out.println(task.getAttribute(nameAttribute).getValue());
			System.out.println(task.getAttribute(epicNumberAttribute).getValue());
			System.out.println(task.getAttribute(referenceAttribute).getValue());
			System.out.println();
		}
		return true;
	}

	// public String getIssueDescription() {
	// MutableIssue issue = issueManager.getIssueObject(issueKey);
	// FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(issue);
	// FieldLayoutItem fieldLayoutItem =
	// fieldLayout.getFieldLayoutItem(IssueFieldConstants.DESCRIPTION);
	// String rendererType = (fieldLayoutItem != null) ?
	// fieldLayoutItem.getRendererType() : null;
	// return rendererManager.getRenderedContent(rendererType,
	// issue.getDescription(), issue.getIssueRenderContext());
	// }
	//
	private static void addJiraLink(Configuration cfg, Issue issue, IServices versionOneServices, Asset newStory)
			throws APIException, ConnectionException {
		IAssetType linkAssetType = versionOneServices.getMeta().getAssetType(LinkType);

		// Asset link to jira,

		IAttributeDefinition nameAttrDef = linkAssetType.getAttributeDefinition("Name");
		IAttributeDefinition urlAttrDef = linkAssetType.getAttributeDefinition("URL");
		IAttributeDefinition onMenuAttrDef = linkAssetType.getAttributeDefinition("OnMenu");
		IAttributeDefinition assetAttrDef = linkAssetType.getAttributeDefinition("Asset");

		// Create and save the link
		Asset linkAsset = new Asset(linkAssetType);
		linkAsset.setAttributeValue(nameAttrDef, cfg.getString(JIRAIssueUrlTitle));
		linkAsset.setAttributeValue(urlAttrDef, cfg.getString(JIRAIssueUrlTemplate) + issue.getKey());
		linkAsset.setAttributeValue(onMenuAttrDef, true);
		linkAsset.setAttributeValue(assetAttrDef, newStory.getOid());
		versionOneServices.save(linkAsset);
	}

	private static void printJiraFields(Issue issue) {

	}

	private static void addEpic(Issue versioneOneServices) {

	}

	private static String txtToHtml(String s) {
		StringBuilder builder = new StringBuilder();
		boolean previousWasASpace = false;
		for (char c : s.toCharArray()) {
			if (c == ' ') {
				if (previousWasASpace) {
					builder.append("&nbsp;");
					previousWasASpace = false;
					continue;
				}
				previousWasASpace = true;
			} else {
				previousWasASpace = false;
			}
			switch (c) {
			case '<':
				builder.append("&lt;");
				break;
			case '>':
				builder.append("&gt;");
				break;
			case '&':
				builder.append("&amp;");
				break;
			case '"':
				builder.append("&quot;");
				break;
			case '\n':
				builder.append("<br>");
				break;
			// We need Tab support here, because we print StackTraces as HTML
			case '\t':
				builder.append("&nbsp; &nbsp; &nbsp;");
				break;
			default:
				builder.append(c);

			}
		}
		String converted = builder.toString();
		String str = "(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:\'\".,<>?«»“”‘’]))";
		Pattern patt = Pattern.compile(str);
		Matcher matcher = patt.matcher(converted);
		converted = matcher.replaceAll("<a href=\"https://$1\">$1</a>");
		return converted;
	}

	private static Configuration readproperties() {

		Configurations configs = new Configurations();
		try {
			Configuration config = configs.xml(new File("VersionOne.Jira.xml"));
			// access configuration properties
			return config;
		} catch (ConfigurationException cex) {
			// Something went wrong
		}
		return null;
	}

}