package vone;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.JiraRestClientFactory;
import com.atlassian.jira.rest.client.domain.BasicComponent;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.versionone.apiclient.exceptions.APIException;
import com.versionone.apiclient.exceptions.ConnectionException;
import com.versionone.apiclient.exceptions.OidException;
import com.versionone.apiclient.exceptions.V1Exception;

public class testJira {

	private static final String Source = "StorySource:5168";
	private static final String Improvement = "StoryCategory:109";
	private static final String NewFeature = "StoryCategory:108";
	private static final String Task = "DefectType:14522";
	private static final String StoryTask = "StoryCategory:15663";
	private static final String DataFix = "DefectType:14708";

	private static final String JIRAIssueUrlTemplate = "Services.JiraService.JIRAIssueUrlTemplate";

	private static final String JIRA_URL = "Services.JiraService.JIRAUrl";
	private static final String JIRA_ADMIN_USERNAME = "Services.JiraService.JIRAUserName";
	private static final String JIRA_ADMIN_PASSWORD = "Services.JiraService.JIRAPassword";
	private static final String JiraDefectFilter = "Services.JiraService.DefectFilter";

	private static final String VersionOneURL = "Services.VersionOneService.Settings.V1Url";
	private static final String VersionOneAccessToken = "Services.VersionOneService.Settings.AccessToken";
	private static final String versionOneProject = "Services.JiraService.ProjectMappings.Mapping.VersionOneProject";
	private static final String JIRAIssueUrlTitle = "Services.JiraService.JIRAIssueUrlTitle";

	private static HashMap<String, String> priorityQMappings, defectPriorityMappings, epicPriorityMappings,
			priorityQBacklogMappings, componentMappings, componentBacklogMappings;

	public static void main(String[] args) throws Exception {

		CommandLine commandLineOptions = processCommandLine(args);

		Configuration cfg = processProperties();

		V1AssetProcessor myv1Asset = versionOneConnection(cfg);

		JiraRestClient jiraClient = jiraConnection(cfg);

		if (commandLineOptions.hasOption("updateepiclinks")) {
			myv1Asset.updateEpicLinks();
			System.exit(0);
		}
		if (commandLineOptions.hasOption("addcomments")) {
			addComments(commandLineOptions, cfg, myv1Asset, jiraClient);
			System.exit(0);
		}
		if (commandLineOptions.hasOption("updateitems")) {
			updateWorkitems(commandLineOptions, cfg, myv1Asset, jiraClient);
			System.exit(0);
		}
		copyWorkitems(commandLineOptions, cfg, myv1Asset, jiraClient);

		// Print the result
		// System.out.println(processedTickets + " " + issue.getSummary() + " "
		// + issue.getWorklogs());
		System.out.println("Finished");
		System.exit(0);
	}

	private static void copyWorkitems(CommandLine commandLineOptions, Configuration cfg, V1AssetProcessor myv1Asset,
			JiraRestClient jiraClient) throws ConnectionException, APIException, OidException, Exception {
		Issue issue = null;
		// SearchResult jiraSearchResult = null;
		boolean moreTickets = true;
		int processedTickets = 0;
		while (moreTickets) {

			SearchResult jiraSearchResult = getTickets(commandLineOptions, cfg, jiraClient, processedTickets);

			for (BasicIssue is : jiraSearchResult.getIssues()) {

				issue = jiraClient.getIssueClient().getIssue(is.getKey()).claim();

				System.out.println("Processing " + issue.getIssueType().getName() + " : " + issue.getKey());

				v1Defect workItem = new v1Defect();

				workItem.setProject(cfg.getString(versionOneProject));
				workItem.setName(issue.getSummary());

				processParentEpic(commandLineOptions, cfg, myv1Asset, jiraClient, issue, workItem);

				if (issue.getIssueType().getName().equals("Bug") && !myv1Asset.checkDefectExists(issue.getKey())
						|| !issue.getIssueType().getName().equals("Bug")
								&& !myv1Asset.checkStoryExists(issue.getKey())) {

					addComments(issue, workItem);
					addVersion(issue, workItem);
					addDescriptions(issue, workItem);
					addITSMReference(issue, workItem);
					addStoryPoints(issue, workItem);
					addStoryType(issue, workItem);
					addPriorityQ(issue, workItem);

					workItem.setPriority(defectPriorityMappings.get(issue.getPriority().getName()).toString());
					workItem.setSource(Source);
					workItem.setFoundBy(issue.getReporter().getDisplayName());
					workItem.setJiraLink(cfg.getString(JIRAIssueUrlTemplate) + issue.getKey());
					workItem.setCreationDate(issue.getCreationDate().toDate());
					workItem.setReference(issue.getKey());

					if (commandLineOptions.hasOption("addcomments")) {
						// myv1Asset.addcomments(defect);
					}
 
					else if (issue.getIssueType().getName().equals("Bug")
							|| issue.getIssueType().getName().equals("Data Fix")) {

						myv1Asset.createDefect(workItem);

					} else if (issue.getIssueType().getName().equals("Epic")) {
						if (myv1Asset.checkEpicExists(issue.getKey()) == null) {
							workItem.setEpic(myv1Asset.createEpic(addEpic(cfg, issue)));
						}
					} else {
						myv1Asset.createStory(workItem);

					}
				}

				processedTickets += 1;
			}
			moreTickets = processedTickets < jiraSearchResult.getTotal();
		}
	}

	private static void updateWorkitems(CommandLine commandLineOptions, Configuration cfg, V1AssetProcessor myv1Asset,
			JiraRestClient jiraClient) throws ConnectionException, APIException, OidException, Exception {
		Issue issue = null;
		// SearchResult jiraSearchResult = null;
		boolean moreTickets = true;
		int processedTickets = 0;
		while (moreTickets) {

			SearchResult jiraSearchResult = getTickets(commandLineOptions, cfg, jiraClient, processedTickets);

			for (BasicIssue is : jiraSearchResult.getIssues()) {

				issue = jiraClient.getIssueClient().getIssue(is.getKey()).claim();

				System.out.println("Processing " + issue.getIssueType().getName() + " : " + issue.getKey());

				v1Defect workItem = new v1Defect();

				workItem.setProject(cfg.getString(versionOneProject));
				workItem.setName(issue.getSummary());
				if (myv1Asset.checkWorkItemExists(issue.getKey())) {
					
				
				if (!issue.getIssueType().getName().equals("Epic")) {

					addComments(issue, workItem);
					addVersion(issue, workItem);
					addITSMReference(issue, workItem);
					addAssignee(issue, workItem);
					addFixVersion(issue, workItem);
					addStoryType(issue, workItem);
					addPriorityQ(issue, workItem);
					addComponent(issue, workItem);

					workItem.setPriority(defectPriorityMappings.get(issue.getPriority().getName()).toString());
					workItem.setSource(Source);
					workItem.setFoundBy(issue.getReporter().getDisplayName());
					workItem.setJiraLink(cfg.getString(JIRAIssueUrlTemplate) + issue.getKey());
					workItem.setCreationDate(issue.getCreationDate().toDate());
					workItem.setReference(issue.getKey());

					if (issue.getIssueType().getName().equals("Bug")
							|| issue.getIssueType().getName().equals("Data Fix")) {

						myv1Asset.updateWorkitem(workItem);

					} else {
						myv1Asset.updateWorkitem(workItem);

					}
				}
				}
				processedTickets += 1;
			}
			moreTickets = processedTickets < jiraSearchResult.getTotal();
		}
	}

	private static void addAssignee(Issue issue, v1Defect workItem) {
		if (issue.getAssignee() != null) {
			workItem.setAssignee(issue.getAssignee().getDisplayName());
		}

	}

	private static void addFixVersion(Issue issue, v1Defect workItem) {
		if (issue.getFixVersions() != null) {
			for (Version version : issue.getFixVersions()) {
				workItem.addFixVersion(version.getName());
			}
		}

	}

	private static void addComments(CommandLine commandLineOptions, Configuration cfg, V1AssetProcessor myv1Asset,
			JiraRestClient jiraClient) throws ConnectionException, APIException, OidException, Exception {
		int processed = 0;
		int withcomments = 0;

		for (Workitem wi : myv1Asset.getWorkitemList()) {
			processed += 1;
			if (processed < 3128)
				continue;
			String filter = "issuekey = '" + wi.getReference() + "'";

			if (wi.getReference().contains(" ")) { // ||
													// !wi.getOid().contains("tory"
				// System.out.println("invalid reference :" +
				// wi.getReference());
			} else {
				SearchResult jiraFind = jiraClient.getSearchClient().searchJql(filter, 1, 0).claim();
				if (jiraFind.getTotal() > 0) {
					for (BasicIssue is : jiraFind.getIssues()) {
						System.out.println("Tickets processed: " + processed + " Withcomments  " + withcomments
								+ " Issue :" + is.getKey());

						Issue issue = jiraClient.getIssueClient().getIssue(is.getKey()).claim();
						addComments(issue, wi);
						if (wi.getItemComments() != null) {
							System.out.println("Comments found for " + wi.getReference());
							withcomments += 1;
							myv1Asset.addcomments(wi);

						}
					}

				}
			}
		}
		return;

	}

	private static Workitem addEpic(Configuration cfg, Issue issue) {
		Workitem epic = new v1Epic();
		epic.setName(issue.getSummary());
		epic.setDescription(issue.getDescription());
		epic.setReference(issue.getKey());
		epic.setJiraLink(cfg.getString(JIRAIssueUrlTemplate) + issue.getKey());
		epic.setProject(cfg.getString(versionOneProject));
		epic.setCreationDate(issue.getCreationDate().toDate());
		epic.setPriority(epicPriorityMappings.get(issue.getPriority().getName()).toString());
		return epic;
	}

	private static void addPriorityQ(Issue issue, v1Defect defect) {
		Iterable<String> priorityQs = issue.getLabels();

		for (String pqs : priorityQs) {
			System.out.println(issue.getKey() + " : " + pqs);
			if (priorityQMappings.get(pqs) != null) {
				defect.addPriorityQ(priorityQMappings.get(pqs));
				defect.addPriorityQBackLogGroup(priorityQBacklogMappings.get(pqs));

			}

		}
	}

	private static void addComponent(Issue issue, v1Defect defect) {
		Iterable<BasicComponent> components = issue.getComponents();

		for (BasicComponent comp : components) {
			System.out.println(issue.getKey() + " : " + comp);
			if (componentMappings.get(comp.getName()) != null) {
				defect.addComponent(componentMappings.get(comp.getName()));
				defect.addComponentBacklogGroup(componentMappings.get(comp.getName()));

			}

		}
	}

	private static void addStoryType(Issue issue, v1Defect defect) {
		if (issue.getIssueType().getName().equals("Improvement")) {
			defect.setStoryType(Improvement);

		}
		if (issue.getIssueType().getName().equals("New Feature")) {
			defect.setStoryType(NewFeature);

		}
		if (issue.getIssueType().getName().equals("Task")) {
			defect.setStoryType(StoryTask);

		}
		if (issue.getIssueType().getName().equals("Data Fix")) {
			defect.setStoryType(DataFix);

		}
	}

	private static void addStoryPoints(Issue issue, v1Defect defect) {
		if (!issue.getIssueType().getName().equals("Epic")) {
			if (issue.getFieldByName("Story Points") != null) {
				if (issue.getFieldByName("Story Points").getValue() != null) {
					defect.setEstimate(issue.getFieldByName("Story Points").getValue().toString());
				}
			}
		}
	}

	private static void addITSMReference(Issue issue, v1Defect defect) {
		if (issue.getFieldByName("ITSM Reference").getValue() != null) {
			defect.setITSMReference(issue.getFieldByName("ITSM Reference").getValue().toString());
		}
	}

	private static void addDescriptions(Issue issue, v1Defect defect) {
		if (issue.getFieldByName("Technical Description").getValue() != null) {
			defect.setDescription(issue.getFieldByName("Technical Description").getValue().toString());
			if (defect.getDescription() != null) {
				defect.setResolution((issue.getDescription()));
			}
		} else {
			if (issue.getDescription() != null) {
				defect.setDescription(issue.getDescription());
			}
		}
	}

	private static void addVersion(Issue issue, v1Defect defect) {
		Iterable<Version> versionList = issue.getFixVersions();
		for (Version f : versionList) {
			System.out.println(f.getName() + " " + f.getDescription());
			defect.setFixedInBuild(f.getDescription());
			if (f.getName().equals("Description")) {
			}
		}
	}

	private static void addComments(Issue issue, Workitem defect) {
		Iterable<Comment> commentList = issue.getComments();
		for (Comment f : commentList) {
			V1Comments comment = new V1Comments();

			comment.setAuthor(f.getAuthor().getName());
			comment.setBody("[" + f.getAuthor().getDisplayName() + "]\n" + f.getBody());
			comment.setAuthoredAt(f.getCreationDate());
			comment.setContent(f.getBody());

			defect.addItemComments(comment);
		}
	}

	private static void processParentEpic(CommandLine commandLineOptions, Configuration cfg, V1AssetProcessor myv1Asset,
			JiraRestClient jiraClient, Issue issue, v1Defect defect)
			throws ConnectionException, APIException, OidException, Exception {
		Issue jiraEpic;
		if (issue.getFieldByName("Epic Link").getValue() == null) {
			defect.setEpic(null);
		} else {
			String jiraEpicName = issue.getFieldByName("Epic Link").getValue().toString();

			jiraEpic = jiraClient.getIssueClient().getIssue(jiraEpicName).claim();

			defect.setEpic(myv1Asset.checkEpicExists(jiraEpic.getKey()));
			Workitem epic = addEpic(cfg, jiraEpic);

			if (defect.getEpic() == null) {
				System.out.println("Creating Epic : " + jiraEpic.getKey());

				defect.setEpic(myv1Asset.createEpic(epic));
			} else if (commandLineOptions.hasOption("updateepic")) {
				System.out.println("Updating Epic : " + jiraEpic.getKey());
				if (commandLineOptions.hasOption("noupdatedescription")) {
					epic.setDescription(null);
				}
				myv1Asset.updateEpic(epic);

			}
		}
	}

	private static SearchResult getTickets(CommandLine commandLineOptions, Configuration cfg, JiraRestClient jiraClient,
			int processedTickets) {
		SearchResult jiraSearchResult;
		if (!commandLineOptions.hasOption("filter")) {
			jiraSearchResult = jiraClient.getSearchClient()
					.searchJql("filter=" + cfg.getString(JiraDefectFilter), 10, processedTickets).claim();
		} else {
			jiraSearchResult = jiraClient.getSearchClient()
					.searchJql(commandLineOptions.getOptionValue("filter"), 50, processedTickets).claim();

		}
		return jiraSearchResult;
	}

	private static V1AssetProcessor versionOneConnection(Configuration cfg) throws MalformedURLException, V1Exception {

		System.out.println("Conencting to version one @ : " + cfg.getString(VersionOneURL));
		V1AssetProcessor myv1Asset = new V1AssetProcessor(cfg.getString(VersionOneURL),
				cfg.getString(VersionOneAccessToken));
		return myv1Asset;
	}

	private static JiraRestClient jiraConnection(Configuration cfg) throws URISyntaxException {
		System.out.println(String.format("Logging in to %s with username '%s' ", cfg.getString(JIRA_URL),
				cfg.getString(JIRA_ADMIN_USERNAME)));

		JiraRestClientFactory jiraClientFactory = new AsynchronousJiraRestClientFactory();

		URI uri = new URI(cfg.getString(JIRA_URL));

		JiraRestClient jiraClient = jiraClientFactory.createWithBasicHttpAuthentication(uri,
				cfg.getString(JIRA_ADMIN_USERNAME), cfg.getString(JIRA_ADMIN_PASSWORD));
		return jiraClient;
	}

	private static Configuration processProperties() {
		Configurations configs = new Configurations();
		try {
			Configuration cfg = configs.xml(new File("VersionOne.Jira.xml"));
			// access configuration properties
			createMappingsMaps(cfg);
			return cfg;
		} catch (ConfigurationException cex) {
			// Something went wrong
		}
		return null;

	}

	private static CommandLine processCommandLine(String[] args) throws ParseException {
		Options options = new Options();

		Option logfile = OptionBuilder.withArgName("filter").hasArg().withDescription("jira filter").create("filter");
		Option updateEpic = new Option("updateepic", "Update Epics only");
		Option noupdatedescription = new Option("noupdatedescription", "Don't update scriptions");
		Option updateepiclinks = new Option("updateepiclinks", "updateepiclinks");
		Option addcomments = new Option("addcomments", "addcomments");
		Option updateitems = new Option("updateitems", "updateitems");
		options.addOption(logfile);
		options.addOption(updateEpic);
		options.addOption(noupdatedescription);
		options.addOption(updateepiclinks);
		options.addOption(addcomments);
		options.addOption(updateitems);

		CommandLineParser parser = new DefaultParser();

		CommandLine commandLineOptions = parser.parse(options, args);

		if (commandLineOptions.hasOption("filter")) {
			System.out.println(commandLineOptions.getOptionValue("filter"));
		}
		if (commandLineOptions.hasOption("updateepic")) {
			System.out.println("Updating Epics");
		}

		if (commandLineOptions.hasOption("noupdatedescription")) {
			System.out.println("No Update Descriptions");
		}

		return commandLineOptions;
	}

	private static void createMappingsMaps(Configuration cfg) {
		if (defectPriorityMappings == null) {
			defectPriorityMappings = new HashMap<String, String>();
		}
		List<HierarchicalConfiguration<ImmutableNode>> fields = ((HierarchicalConfiguration<ImmutableNode>) cfg)
				.configurationsAt("Services.JiraService.PriorityMappings.Mapping");
		for (@SuppressWarnings("rawtypes")
		HierarchicalConfiguration sub : fields) {
			defectPriorityMappings.put((String) sub.getString("JIRAPriority"),
					(String) sub.getString("VersionOnePriority"));
		}
		if (epicPriorityMappings == null) {
			epicPriorityMappings = new HashMap<String, String>();
		}
		fields = ((HierarchicalConfiguration<ImmutableNode>) cfg)
				.configurationsAt("Services.JiraService.EpicPriorityMappings.Mapping");
		for (@SuppressWarnings("rawtypes")
		HierarchicalConfiguration sub : fields) {
			epicPriorityMappings.put((String) sub.getString("JIRAPriority"),
					(String) sub.getString("VersionOnePriority"));
		}
		if (priorityQMappings == null) {
			priorityQMappings = new HashMap<String, String>();
		}
		fields = ((HierarchicalConfiguration<ImmutableNode>) cfg)
				.configurationsAt("Services.JiraService.PriorityQMappings.Mapping");
		for (@SuppressWarnings("rawtypes")
		HierarchicalConfiguration sub : fields) {
			priorityQMappings.put((String) sub.getString("JIRAPriority"), (String) sub.getString("VersionOnePriority"));
		}
		if (priorityQBacklogMappings == null) {
			priorityQBacklogMappings = new HashMap<String, String>();
		}
		fields = ((HierarchicalConfiguration<ImmutableNode>) cfg)
				.configurationsAt("Services.JiraService.PriorityQMappings.Mapping");
		for (@SuppressWarnings("rawtypes")
		HierarchicalConfiguration sub : fields) {
			priorityQBacklogMappings.put((String) sub.getString("JIRAPriority"),
					(String) sub.getString("BacklogGroup"));
		}
		if (componentMappings == null) {
			componentMappings = new HashMap<String, String>();
		}

		fields = ((HierarchicalConfiguration<ImmutableNode>) cfg)
				.configurationsAt("Services.JiraService.componentMappings.Mapping");
		for (@SuppressWarnings("rawtypes")
		HierarchicalConfiguration sub : fields) {
			componentMappings.put((String) sub.getString("JIRAComponent"),
					(String) sub.getString("VersionOneComponent"));
		}
		if (componentBacklogMappings == null) {
			componentBacklogMappings = new HashMap<String, String>();
		}
		fields = ((HierarchicalConfiguration<ImmutableNode>) cfg)
				.configurationsAt("Services.JiraService.componentMappings.Mapping");
		for (@SuppressWarnings("rawtypes")
		HierarchicalConfiguration sub : fields) {
			componentBacklogMappings.put((String) sub.getString("JIRAComponent"),
					(String) sub.getString("BacklogGroup"));
		}

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
