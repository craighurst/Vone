package vone;

import java.net.MalformedURLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.versionone.DB.DateTime;
import com.versionone.Oid;
import com.versionone.apiclient.Asset;
import com.versionone.apiclient.Paging;
import com.versionone.apiclient.Query;
import com.versionone.apiclient.Services;
import com.versionone.apiclient.TokenTerm;
import com.versionone.apiclient.V1Connector;
import com.versionone.apiclient.exceptions.APIException;
import com.versionone.apiclient.exceptions.ConnectionException;
import com.versionone.apiclient.exceptions.OidException;
import com.versionone.apiclient.exceptions.V1Exception;
import com.versionone.apiclient.filters.FilterTerm;
import com.versionone.apiclient.interfaces.IAssetType;
import com.versionone.apiclient.interfaces.IAttributeDefinition;
import com.versionone.apiclient.interfaces.IOperation;
import com.versionone.apiclient.interfaces.IServices;
import com.versionone.apiclient.services.QueryFind;
import com.versionone.apiclient.services.QueryResult;

public class V1AssetProcessor {
	private String VersionOneURL;
	private String VersionOneAccessToken;
	private String versionOneProject;
	private String JIRAIssueUrlTitle;
	private V1Connector versionOneConnector;
	private IServices versionOneServices;
	private Oid versionOneProjectOid;
	private String Name;
	private IAssetType epicType, defectType, linkAssetType, expressionAssetType, storyType;

	private static final String EpicType = "Epic";
	private static final String MemberType = "Member";
	private static final String workItemType = "Workitem";
	private static final String DefectType = "Defect";
	private static final String LinkType = "Link";
	private static final String StoryType = "Story";
	private static final String expressionType = "Expression";
	private IAttributeDefinition defectPriorityAttribute, defectReferenceAttribute, defectEpicNumberAttribute,
			defectNameAttribute, defectDescriptionAttribute, defectResolutionAttribute, defectFixedInBuildAttribute,
			defectSourceAttribute, defectFoundByAttribute, defectSuperAttribute, defectCustom_JiraDefectCreationDate,
			defectParentAttribute, defectEstimateAttribute, defectCustom_ITSMReference, defectTypeAttribute;

	private IAttributeDefinition storyPriorityAttribute, storyReferenceAttribute, storyEpicNumberAttribute,
			storyNameAttribute, storyDescriptionAttribute, storySourceAttribute, storySuperAttribute,
			storyCustom_JirastoryCreationDate, storyParentAttribute, storyEstimateAttribute, storyCustom_ITSMReference,
			storyTypeAttribute;

	private IAttributeDefinition epicPriorityAttribute, epicReferenceAttribute, epicEpicNumberAttribute,
			epicNameAttribute, epicDescriptionAttribute, epicResolutionAttribute, epicFixedInBuildAttribute,
			epicSourceAttribute, epicFoundByAttribute, epicSuperAttribute, epicCustom_JiraepicCreationDate;

	private IAttributeDefinition linkNameAttribute, linkURLAttribute, linkOnMenuAttribute, linkAssetAttribute;
	private Logger logger;
	private HashMap epicMap;

	public V1AssetProcessor() {
		setUpLogging();

		System.out.println("Constructed");

	}

	public V1AssetProcessor(String v1Url, String v1AccessToken) throws MalformedURLException, V1Exception {

		setUpLogging();

		setVersionOneURL(v1Url);

		setVersionOneAccessToken(v1AccessToken);

		this.versionOneConnector = V1Connector.withInstanceUrl(getVersionOneURL()).withUserAgentHeader("AppName", "1.0")
				.withAccessToken(getVersionOneAccessToken()).build();
		this.versionOneServices = new Services(versionOneConnector);

		this.epicType = getEpicType();
		this.defectType = getDefectType();
		System.out.println("Constructed");

	}

	private void setUpLogging() {
		logger = LoggerFactory.getLogger(V1AssetProcessor.class);
	}

	public String getVersionOneURL() {
		return VersionOneURL;
	}

	public void setVersionOneURL(String versionOneURL) {
		VersionOneURL = versionOneURL;
	}

	public String getVersionOneAccessToken() {
		return VersionOneAccessToken;
	}

	public void setVersionOneAccessToken(String versionOneAccessToken) {
		VersionOneAccessToken = versionOneAccessToken;
	}

	public String getVersionOneProject() {
		return versionOneProject;
	}

	public void setVersionOneProject(String versionOneProject) {
		this.versionOneProject = versionOneProject;
		try {
			this.versionOneProjectOid = versionOneServices.getOid(this.versionOneProject);
		} catch (OidException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getJIRAIssueUrlTitle() {
		return JIRAIssueUrlTitle;
	}

	public void setJIRAIssueUrlTitle(String jIRAIssueUrlTitle) {
		JIRAIssueUrlTitle = jIRAIssueUrlTitle;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public void createDefect(v1Defect defect) throws Exception {
		IAssetType assetType = getDefectType();

		System.out.println(defect.getReference() + " to be created");

		try {
			Asset newAsset = versionOneServices.createNew(assetType, versionOneServices.getOid(defect.getProject()));
			newAsset.setAttributeValue(getDefectNameAttribute(), defect.getName());
			if (defect.getDescription() != null) {
				newAsset.setAttributeValue(getDefectDescriptionAttribute(),
						txtToHtml(Normalizer.normalize(defect.getDescription(), Normalizer.Form.NFC)));
			}

			newAsset.setAttributeValue(getDefectCustom_JiraDefectCreationDate(), defect.getCreationDate());
			if (defect.getResolution() != null) {
				newAsset.setAttributeValue(getDefectResolutionAttribute(),
						txtToHtml(defect.getResolution().replaceAll("[^\\x00-\\x7F]", "")));
			}

			newAsset.setAttributeValue(getDefectFixedInBuildAttribute(), defect.getFixedInBuild());
			newAsset.setAttributeValue(getDefectReferenceAttribute(), defect.getReference());
			newAsset.setAttributeValue(getDefectPriorityAttribute(), defect.getPriority());
			newAsset.setAttributeValue(getDefectSourceAttribute(), defect.getSource());
			newAsset.setAttributeValue(getDefectFoundByAttribute(), defect.getFoundBy());
			newAsset.setAttributeValue(getDefectCustom_ITSMReference(), defect.getITSMReference());

			newAsset.setAttributeValue(getDefectEstimateAttribute(), "1");

			if (defect.getStoryType() != null) {
				newAsset.setAttributeValue(getDefectTypeAttribute(), defect.getStoryType());
			}

			if (defect.getPriorityQ() != null) {
				for (String pqs : defect.getPriorityQ()) {

					System.out.println(pqs);
					newAsset.addAttributeValue(getDefectType().getAttributeDefinition("Custom_PriorityQ2"), pqs);

				}
			}

			if (defect.getPriorityQBacklog() != null) {
				for (String pqs : defect.getPriorityQBacklog()) {

					System.out.println(pqs);
					newAsset.setAttributeValue(getDefectParentAttribute(), pqs);
					break;

				}
			}

			if (defect.getEpic() != null) {

				newAsset.setAttributeValue(getDefectSuperAttribute(), versionOneServices.getOid(defect.getEpic()));
			}
			versionOneServices.save(newAsset);

			IAssetType linkAssetType = getLinkAssetType();

			// Create and save the link
			Asset linkAsset = new Asset(linkAssetType);
			linkAsset.setAttributeValue(getLinkNameAttribute(), "Jira");
			linkAsset.setAttributeValue(getLinkURLAttribute(), defect.getJiraLink());
			linkAsset.setAttributeValue(getLinkOnMenuAttribute(), true);
			linkAsset.setAttributeValue(getLinkAssetAttribute(), newAsset.getOid());
			versionOneServices.save(linkAsset);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void createStory(v1Defect defect) throws Exception {
		IAssetType assetType = getStoryType();

		System.out.println("Story to be created : " + defect.getReference());

		try {
			Asset newAsset = versionOneServices.createNew(assetType, versionOneServices.getOid(defect.getProject()));
			newAsset.setAttributeValue(getStoryNameAttribute(), defect.getName());
			if (defect.getDescription() != null) {
				newAsset.setAttributeValue(getStoryDescriptionAttribute(),
						txtToHtml(Normalizer.normalize(defect.getDescription(), Normalizer.Form.NFC)));
			}

			newAsset.setAttributeValue(getStoryCustom_JiraStoryCreationDate(), defect.getCreationDate());
			// if (defect.getResolution() != null) {
			// newAsset.setAttributeValue(getStoryResolutionAttribute(),
			// txtToHtml(defect.getResolution().replaceAll("[^\\x00-\\x7F]",
			// "")));
			// }

			// newAsset.setAttributeValue(getStoryFixedInBuildAttribute(),
			// defect.getFixedInBuild());
			newAsset.setAttributeValue(getStoryReferenceAttribute(), defect.getReference());
			newAsset.setAttributeValue(getStoryPriorityAttribute(), defect.getPriority());
			newAsset.setAttributeValue(getStorySourceAttribute(), defect.getSource());
			newAsset.setAttributeValue(getStoryCustom_ITSMReference(), defect.getITSMReference());
			newAsset.setAttributeValue(getStoryEstimateAttribute(), defect.getEstimate());
			if (defect.getStoryType() != null) {
				newAsset.setAttributeValue(getStoryTypeAttribute(), defect.getStoryType());
			}

			if (defect.getEpic() != null) {

				newAsset.setAttributeValue(getStorySuperAttribute(), versionOneServices.getOid(defect.getEpic()));
			}
			versionOneServices.save(newAsset);

			IAssetType linkAssetType = getLinkAssetType();

			// Create and save the link
			Asset linkAsset = new Asset(linkAssetType);
			linkAsset.setAttributeValue(getLinkNameAttribute(), "Jira");
			linkAsset.setAttributeValue(getLinkURLAttribute(), defect.getJiraLink());
			linkAsset.setAttributeValue(getLinkOnMenuAttribute(), true);
			linkAsset.setAttributeValue(getLinkAssetAttribute(), newAsset.getOid());
			System.out.println("Newasset oid : " + newAsset.getOid() + " Token : " + newAsset.getOid().getToken());
			versionOneServices.save(linkAsset);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void updateWorkitem(v1Defect defect) throws Exception {
		IAssetType assetType = versionOneServices.getMeta().getAssetType(workItemType);
		Query query = new Query(assetType);
		Asset asset = null;
		query.getSelection()
				.add(versionOneServices.getMeta().getAssetType(workItemType).getAttributeDefinition("Reference"));
		// query.getSelection().add(versionOneServices.getMeta().getAssetType(workItemType).getAttributeDefinition("Custom_ITSMReference2"));
		// query.getSelection().add(versionOneServices.getMeta().getAssetType(workItemType).getAttributeDefinition("Category"));
		// query.getSelection().add(versionOneServices.getMeta().getAssetType(workItemType).getAttributeDefinition("WorkitemPriority"));
		query.getSelection().add(getStoryEpicNumberAttribute());
		FilterTerm referenceTerm = new FilterTerm(
				versionOneServices.getMeta().getAssetType(workItemType).getAttributeDefinition("Reference"));
		referenceTerm.equal(defect.getReference());
		query.setFilter(referenceTerm);
		QueryResult result = versionOneServices.retrieve(query);
		if (result.getTotalAvaliable() > 0) {
			asset = result.getAssets()[0];
		}

		Asset assignee = getAssignee(defect);
		Asset fv = getFixVersion(defect);

		try {
			System.out.println(asset.getAssetType().getToken());
			// asset.setAttributeValue(versionOneServices.getMeta().getAssetType(workItemType).getAttributeDefinition("WorkitemPriority"),
			// defect.getPriority());
			// asset.setAttributeValue(versionOneServices.getMeta().getAttributeDefinition("Defect.Custom_ITSMReference2"),
			// defect.getITSMReference());
			String assetTypeName = asset.getAssetType().getToken();

			String EstimateName = null;
			String WorkitemType = null;
			String Owners = null;
			String FixVersion = null;
			String Parent = null;
			String Component = null;
			String PriorityQName = null;

			switch (assetTypeName) {
			case "Defect": {
				EstimateName = assetTypeName + ".Estimate";
				WorkitemType = assetTypeName + ".Type";
				Owners = assetTypeName + ".Owners";
				FixVersion = assetTypeName + ".Custom_Fix_Version";
				Parent = assetTypeName + ".Parent";
				Component = assetTypeName + ".Custom_Component3";
				PriorityQName = assetTypeName + ".Custom_PriorityQ2";
				break;
			}
			case "Story": {
				EstimateName = assetTypeName + ".Estimate";
				WorkitemType = assetTypeName + ".Category";
				Owners = assetTypeName + ".Owners";
				FixVersion = assetTypeName + ".Custom_Fix_Version2";
				Parent = assetTypeName + ".Parent";
				Component = assetTypeName + ".Custom_Component4";
				break;
			}
			case "Epic": {

			}
			}
			if (defect.getEstimate() != null) {

				asset.setAttributeValue(versionOneServices.getMeta().getAttributeDefinition(EstimateName),
						defect.getEstimate());
			}
			if (defect.getStoryType() != null) {

				asset.setAttributeValue(versionOneServices.getMeta().getAttributeDefinition(WorkitemType),
						defect.getStoryType());
			}

			if (assignee != null) {
				asset.addAttributeValue(versionOneServices.getMeta().getAttributeDefinition(Owners), assignee.getOid());

			}
			if (fv != null) {
				asset.addAttributeValue(versionOneServices.getMeta().getAttributeDefinition(FixVersion), fv.getOid());

			}

			if (defect.getPriorityQ() != null) {
				for (String pqs : defect.getPriorityQ()) {

					System.out.println(pqs);
					asset.addAttributeValue(versionOneServices.getAttributeDefinition(PriorityQName), pqs);

				}
			}

			if (defect.getPriorityQBacklog() != null) {
				for (String pqs : defect.getPriorityQBacklog()) {

					System.out.println(pqs);
					asset.setAttributeValue(versionOneServices.getAttributeDefinition(Parent), pqs);
					break;

				}
			}
			if (defect.getComponents() != null) {
				for (String pqs : defect.getComponents()) {

					System.out.println(pqs);
					asset.addAttributeValue(versionOneServices.getAttributeDefinition(Component), pqs);

				}
			}

			if (defect.getEpic() != null) {

				asset.setAttributeValue(getStorySuperAttribute(), versionOneServices.getOid(defect.getEpic()));
			}
			versionOneServices.save(asset);
			System.out.println("Newasset oid : " + asset.getOid() + " Token : " + asset.getOid().getToken());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Asset getAssignee(v1Defect defect) throws ConnectionException, APIException, OidException {
		Query query;
		FilterTerm referenceTerm;
		QueryResult result;
		IAssetType member = versionOneServices.getMeta().getAssetType(MemberType);
		query = new Query(member);
		Asset assignee = null;
		query.getSelection().add(versionOneServices.getMeta().getAttributeDefinition("Member.Name"));
		referenceTerm = new FilterTerm(
				versionOneServices.getMeta().getAssetType(workItemType).getAttributeDefinition("Reference"));
		referenceTerm.equal(defect.getReference());
		query.setFind(new QueryFind(defect.getAssignee()));
		result = versionOneServices.retrieve(query);
		if (result.getTotalAvaliable() > 0) {
			assignee = result.getAssets()[0];
		}
		return assignee;
	}

	private Asset getFixVersion(v1Defect defect) throws V1Exception {
		Query query;
		FilterTerm referenceTerm;
		QueryResult result;
		IAssetType fv = versionOneServices.getMeta().getAssetType("Custom_Fix_version");
		if (defect.getFixVersions() == null) {
			return null;
		}
		for (String version : defect.getFixVersions()) {

			query = new Query(fv);
			Asset fv_value = null;
			query.getSelection().add(versionOneServices.getMeta().getAttributeDefinition("Custom_Fix_version.Name"));
			query.getSelection()
					.add(versionOneServices.getMeta().getAttributeDefinition("Custom_Fix_version.Description"));
			referenceTerm = new FilterTerm(
					versionOneServices.getMeta().getAttributeDefinition("Custom_Fix_version.Name"));
			referenceTerm.equal(version);
			query.setFilter(referenceTerm);
			result = versionOneServices.retrieve(query);
			if (result.getTotalAvaliable() > 0) {
				fv_value = result.getAssets()[0];
			} else {
				fv_value = versionOneServices.createNew(fv, versionOneServices.getOid(defect.getProject()));
				fv_value.setAttributeValue(
						versionOneServices.getMeta().getAttributeDefinition("Custom_Fix_version.Name"), version);
				versionOneServices.save(fv_value);

			}
			return fv_value;
		}
		return null;
	}

	public boolean checkStoryExists(String defect) throws ConnectionException, APIException, OidException {
		Query query = new Query(getStoryType());

		query.getSelection().add(getStoryReferenceAttribute());
		query.getSelection().add(getStoryNameAttribute());
		query.getSelection().add(getStoryEpicNumberAttribute());
		FilterTerm referenceTerm = new FilterTerm(getStoryReferenceAttribute());
		referenceTerm.equal(defect);
		query.setFilter(referenceTerm);
		QueryResult result = versionOneServices.retrieve(query);
		if (result.getTotalAvaliable() > 0) {
			System.out.println("Story already created before this run : " + defect);

			return true;
		} else
			return false;
	}

	public boolean checkDefectExists(String defect) throws ConnectionException, APIException, OidException {
		Query query = new Query(getDefectType());

		query.getSelection().add(getDefectReferenceAttribute());
		query.getSelection().add(getDefectNameAttribute());
		query.getSelection().add(getDefectEpicNumberAttribute());
		FilterTerm referenceTerm = new FilterTerm(getDefectReferenceAttribute());
		referenceTerm.equal(defect);
		query.setFilter(referenceTerm);
		QueryResult result = versionOneServices.retrieve(query);
		if (result.getTotalAvaliable() > 0) {
			System.out.println("Defect already created before this run : " + defect);

			return true;
		} else
			return false;
	}

	public String createEpic(Workitem epic) throws Exception {

		// getcomments();
		// checkEpicExists(epic);
		Asset newAsset = versionOneServices.createNew(getEpicType(), versionOneServices.getOid(epic.getProject()));

		System.out.println("Epic to be created : " + epic.getReference());

		newAsset.setAttributeValue(getEpicNameAttribute(), epic.getName());
		if (epic.getDescription() != null) {
			newAsset.setAttributeValue(getEpicDescriptionAttribute(),
					txtToHtml(Normalizer.normalize(epic.getDescription(), Normalizer.Form.NFC)));
			//
		}
		newAsset.setAttributeValue(getEpicReferenceAttribute(), epic.getReference());
		newAsset.setAttributeValue(getEpicPriorityAttribute(), epic.getPriority());
		newAsset.setAttributeValue(getEpicCustom_JiraepicCreationDate(), epic.getCreationDate());

		versionOneServices.save(newAsset);

		addEpicLink(epic, newAsset);
		// System.out.println(newAsset.getAttribute(getEpicEpicNumberAttribute()).getValue().toString());
		cacheEpic(epic.getReference(), newAsset.getOid().toString());

		return newAsset.getOid().toString();

	}

	private void addEpicLink(Workitem epic, Asset newAsset) throws APIException, ConnectionException {
		IAssetType linkAssetType = getLinkAssetType();

		Asset linkAsset = new Asset(linkAssetType);
		linkAsset.setAttributeValue(getLinkNameAttribute(), "Jira");
		linkAsset.setAttributeValue(getLinkURLAttribute(), epic.getJiraLink());
		linkAsset.setAttributeValue(getLinkOnMenuAttribute(), true);
		linkAsset.setAttributeValue(getLinkAssetAttribute(), newAsset.getOid());
		versionOneServices.save(linkAsset);
	}

	private void updateEpicLink(Workitem epic, Asset newAsset) throws APIException, ConnectionException, OidException {
		// IAssetType linkAssetType = ;

		Query query = new Query(getLinkAssetType());

		query.getSelection().add(getLinkNameAttribute());
		query.getSelection().add(getLinkURLAttribute());
		query.getSelection().add(getLinkOnMenuAttribute());
		query.getSelection().add(getLinkOnMenuAttribute());
		query.getSelection().add(getEpicPriorityAttribute());
		query.getSelection().add(getLinkAssetAttribute());
		FilterTerm referenceTerm = new FilterTerm(getLinkAssetAttribute());
		referenceTerm.equal(newAsset.getOid());
		query.setFilter(referenceTerm);
		QueryResult result = versionOneServices.retrieve(query);
		if (result.getTotalAvaliable() > 0) {
			Asset link = result.getAssets()[0];

			link.setAttributeValue(getLinkNameAttribute(), "Jira");
			link.setAttributeValue(getLinkURLAttribute(), epic.getJiraLink());
			link.setAttributeValue(getLinkOnMenuAttribute(), true);
			link.setAttributeValue(getLinkAssetAttribute(), newAsset.getOid());
			versionOneServices.save(link);
		} else
			addEpicLink(epic, newAsset);
	}

	public void updateEpic(Workitem epic) throws Exception {

		Query query = new Query(getEpicType());

		query.getSelection().add(getEpicReferenceAttribute());
		query.getSelection().add(getEpicNameAttribute());
		query.getSelection().add(getEpicEpicNumberAttribute());
		query.getSelection().add(getEpicDescriptionAttribute());
		query.getSelection().add(getEpicPriorityAttribute());
		query.getSelection().add(getEpicCustom_JiraepicCreationDate());
		FilterTerm referenceTerm = new FilterTerm(getEpicReferenceAttribute());
		referenceTerm.equal(epic.getReference());
		query.setFilter(referenceTerm);
		QueryResult result = versionOneServices.retrieve(query);

		Asset newAsset = result.getAssets()[0];

		newAsset.setAttributeValue(getEpicNameAttribute(), epic.getName());
		if (epic.getDescription() != null) {
			newAsset.setAttributeValue(getEpicDescriptionAttribute(), txtToHtml(epic.getDescription()));
			// txtToHtml(Normalizer.normalize(epic.getDescription(),
			// Normalizer.Form.NFC)));
			//
		}
		newAsset.setAttributeValue(getEpicReferenceAttribute(), epic.getReference());
		newAsset.setAttributeValue(getEpicPriorityAttribute(), epic.getPriority());
		newAsset.setAttributeValue(getEpicCustom_JiraepicCreationDate(), epic.getCreationDate());

		versionOneServices.save(newAsset);
		updateEpicLink(epic, newAsset);

		// System.out.println(newAsset.getAttribute(getEpicEpicNumberAttribute()).getValue().toString());
		cacheEpic(epic.getReference(), newAsset.getOid().toString());

	}

	public String checkEpicExists(String epicName) throws ConnectionException, APIException, OidException {
		if (checkEpicMap(epicName)) {
			System.out.println("Epic created in this run already : " + epicName);

			return getEpicMap(epicName);
		}

		Query query = new Query(getEpicType());

		query.getSelection().add(getEpicReferenceAttribute());
		query.getSelection().add(getEpicNameAttribute());
		query.getSelection().add(getEpicEpicNumberAttribute());
		FilterTerm referenceTerm = new FilterTerm(getEpicReferenceAttribute());
		referenceTerm.equal(epicName);
		query.setFilter(referenceTerm);
		QueryResult result = versionOneServices.retrieve(query);
		if (result.getTotalAvaliable() > 0) {

			for (Asset resultEpic : result.getAssets()) {
				System.out.println("Epic already created before this run : " + epicName);
				System.out.println(resultEpic.getOid().toString());
				cacheEpic(epicName, resultEpic.getOid().getToken());

				return resultEpic.getOid().getToken();
			}
		}
		return null;
	}

	public void updateEpicLinks() throws ConnectionException, APIException, OidException {
		Query query = new Query(getEpicType());
		System.out.println("Updating Links : ");

		query.getSelection().add(getEpicReferenceAttribute());
		query.getSelection().add(getEpicNameAttribute());
		query.getSelection().add(getEpicEpicNumberAttribute());
		query.getSelection().add(getLinkAssetAttribute());
		// query.getSelection().add(getLinkAssetAttribute());
		FilterTerm referenceTerm = new FilterTerm(getEpicReferenceAttribute());
		referenceTerm.exists();
		query.setFilter(referenceTerm);
		QueryResult result = versionOneServices.retrieve(query);
		Workitem epic = new v1Epic();

		if (result.getTotalAvaliable() > 0) {

			for (Asset link : result.getAssets()) {
				System.out.println(link.getAttribute(getEpicNameAttribute()).getValue().toString() + " "
						+ link.getAttribute(getEpicReferenceAttribute()).getValue().toString());

				epic.setJiraLink("https://shortcuts.atlassian.net/browse/"
						+ link.getAttribute(getEpicReferenceAttribute()).getValue());
				updateEpicLink(epic, link);

			}
		}
	}

	public List<Workitem> getWorkitemList() throws ConnectionException, APIException, OidException {
		Query query = new Query(versionOneServices.getMeta().getAssetType(workItemType));
		System.out.println("getWorkitemList : ");

		query.getSelection()
				.add(versionOneServices.getMeta().getAssetType(workItemType).getAttributeDefinition("Reference"));
		query.getSelection()
				.add(versionOneServices.getMeta().getAssetType(workItemType).getAttributeDefinition("Name"));
		query.getSelection()
				.add(versionOneServices.getMeta().getAssetType(workItemType).getAttributeDefinition("Number"));
		query.getSelection()
				.add(versionOneServices.getMeta().getAssetType(workItemType).getAttributeDefinition("AssetType"));
		// query.getSelection().add(versionOneServices.getMeta().getAssetType(workItemType).getAttributeDefinition("Asset"));
		// query.getSelection().add(getLinkAssetAttribute());
		FilterTerm referenceTerm = new FilterTerm(
				versionOneServices.getMeta().getAssetType(workItemType).getAttributeDefinition("Reference"));
		referenceTerm.exists();
		query.setFilter(referenceTerm);
		query.setPaging(new Paging(0, 5500));
		QueryResult result = versionOneServices.retrieve(query);
		List<Workitem> workitems = new ArrayList<Workitem>();
		WorkItemFactory wif = new WorkItemFactory();
		if (result.getTotalAvaliable() > 0) {

			for (Asset wi : result.getAssets()) {
				Workitem wItem = wif.getWorkitem(wi.getAssetType().getToken());
				wItem.setReference(wi.getAttribute(
						versionOneServices.getMeta().getAssetType(workItemType).getAttributeDefinition("Reference"))
						.getValue().toString());
				wItem.setOid(wi.getOid().getToken());

				workitems.add(wItem);
				// System.out
				// .println(wi
				// .getAttribute(versionOneServices.getMeta().getAssetType(workItemType)
				// .getAttributeDefinition("Name"))
				// .getValue().toString()
				// + " "
				// +
				// wi.getAttribute(versionOneServices.getMeta().getAssetType(workItemType)
				// .getAttributeDefinition("Reference")).getValue().toString()
				// + " "
				// +
				// wi.getAttribute(versionOneServices.getMeta().getAssetType(workItemType)
				// .getAttributeDefinition("Number")).getValue().toString()
				// + " " + wi.getAssetType().getDisplayName() + " "
				// + wi.getAssetType().getNameAttribute().toString()
				// + wi.getOid().getToken());
				// epic.setJiraLink("https://shortcuts.atlassian.net/browse/"
				// + link.getAttribute(getEpicReferenceAttribute()).getValue());
				// updateEpicLink(epic, link);

			}
		}
		return workitems;

	}

	public void addcomments(Workitem workitem) throws V1Exception {
		// TODO Auto-generated method stub

		System.out.println("Workitem " + workitem.getReference());

		IAssetType conversationType = versionOneServices.getMeta().getAssetType("Conversation");
		IAssetType expressionType = versionOneServices.getMeta().getAssetType("Expression");
		IAttributeDefinition authorAttribute = expressionType.getAttributeDefinition("Author");
		IAttributeDefinition authoredAtAttribute = expressionType.getAttributeDefinition("AuthoredAt");
		IAttributeDefinition contentAttribute = expressionType.getAttributeDefinition("Content");
		IAttributeDefinition belongsToAttribute = expressionType.getAttributeDefinition("BelongsTo");
		IAttributeDefinition inReplyToAttribute = expressionType.getAttributeDefinition("InReplyTo");
		IAttributeDefinition mentionsAttribute = expressionType.getAttributeDefinition("Mentions");

		Query query = new Query(expressionType);
		System.out.println("Getting existing conversations : ");

		query.getSelection().add(contentAttribute);
		query.getSelection().add(belongsToAttribute);
		query.getSelection().add(authorAttribute);
		// query.getSelection()
		// .add(versionOneServices.getMeta().getAssetType(workItemType).getAttributeDefinition("Number"));
		// query.getSelection()
		// .add(versionOneServices.getMeta().getAssetType(workItemType).getAttributeDefinition("AssetType"));
		// query.getSelection().add(versionOneServices.getMeta().getAssetType(workItemType).getAttributeDefinition("Asset"));
		// query.getSelection().add(getLinkAssetAttribute());
		TokenTerm tokenTerm = new TokenTerm("Mentions='" + workitem.getOid() + "'");
		// referenceTerm.equals(versionOneServices.getOid(workitem.getOid()));
		query.setFilter(tokenTerm);

		QueryResult result = versionOneServices.retrieve(query);

		IOperation deleteOperation = versionOneServices.getMeta().getAssetType("Expression").getOperation("Delete");

		for (Asset expr : result.getAssets()) {
			// System.out.println(expr.getAttributes().toString());
			Oid deletedOID = versionOneServices.executeOperation(deleteOperation, expr.getOid());

		}

		for (V1Comments f : workitem.getItemComments()) {
			Asset newConversation = versionOneServices.createNew(conversationType, null);
			Asset questionExpression = versionOneServices.createNew(expressionType, null);
			versionOneServices.save(newConversation);

			// questionExpression.setAttributeValue(authorAttribute,
			// versionOneServices.getOid("Member:1539"));
			questionExpression.setAttributeValue(authoredAtAttribute, f.getAuthoredAt().toDate());
			questionExpression.setAttributeValue(contentAttribute, f.getBody().replaceAll("\r", "")); // Normalizer.normalize(defect.getDescription(),
																										// Normalizer.Form.NFC))
			questionExpression.setAttributeValue(belongsToAttribute, newConversation.getOid());
			questionExpression.addAttributeValue(mentionsAttribute, versionOneServices.getOid(workitem.getOid()));
			versionOneServices.save(questionExpression);
			versionOneServices.save(newConversation);

		}

	}

	public void addcomment() throws V1Exception {
		// TODO Auto-generated method stub

		Query query = new Query(getEpicType());
		System.out.println("Updating Links : ");

		query.getSelection().add(getEpicReferenceAttribute());
		query.getSelection().add(getEpicNameAttribute());
		query.getSelection().add(getEpicEpicNumberAttribute());
		query.getSelection().add(getLinkAssetAttribute());
		// query.getSelection().add(getLinkAssetAttribute());
		FilterTerm referenceTerm = new FilterTerm(getEpicReferenceAttribute());
		referenceTerm.exists();
		query.setFilter(referenceTerm);
		QueryResult result = versionOneServices.retrieve(query);
		Workitem epic = new v1Epic();

		if (result.getTotalAvaliable() > 0) {

			for (Asset link : result.getAssets()) {

				System.out.println("Epic " + link.getAttribute(getEpicNameAttribute()).getValue().toString());

				IAssetType conversationType = versionOneServices.getMeta().getAssetType("Conversation");
				IAssetType expressionType = versionOneServices.getMeta().getAssetType("Expression");
				IAttributeDefinition authorAttribute = expressionType.getAttributeDefinition("Author");
				IAttributeDefinition authoredAtAttribute = expressionType.getAttributeDefinition("AuthoredAt");
				IAttributeDefinition contentAttribute = expressionType.getAttributeDefinition("Content");
				IAttributeDefinition belongsToAttribute = expressionType.getAttributeDefinition("BelongsTo");
				IAttributeDefinition inReplyToAttribute = expressionType.getAttributeDefinition("InReplyTo");
				IAttributeDefinition mentionsAttribute = expressionType.getAttributeDefinition("Mentions");
				Asset newConversation = versionOneServices.createNew(conversationType, null);
				Asset questionExpression = versionOneServices.createNew(expressionType, null);
				versionOneServices.save(newConversation);

				questionExpression.setAttributeValue(authorAttribute, versionOneServices.getOid("Member:1539"));
				questionExpression.setAttributeValue(authoredAtAttribute, DateTime.now());
				questionExpression.setAttributeValue(contentAttribute, "Is this a test conversation?");
				questionExpression.setAttributeValue(belongsToAttribute, newConversation.getOid());
				questionExpression.addAttributeValue(mentionsAttribute, link.getOid());
				versionOneServices.save(questionExpression);

				Asset answerExpression = versionOneServices.createNew(expressionType, questionExpression.getOid());
				answerExpression.setAttributeValue(authorAttribute, versionOneServices.getOid("Member:1539"));
				answerExpression.setAttributeValue(authoredAtAttribute,
						DateUtils.addMinutes(DateTime.now().getValue(), 15));
				answerExpression.setAttributeValue(contentAttribute, "Yes it is!");
				answerExpression.setAttributeValue(inReplyToAttribute, questionExpression.getOid());
				versionOneServices.save(answerExpression);

			}
		}

	}

	private String getEpicMap(String reference) {
		// TODO Auto-generated method stub
		// System.out.println((String) epicMap.get(reference));
		return (String) epicMap.get(reference);

	}

	private boolean checkEpicMap(String reference) {
		// TODO Auto-generated method stub
		checkMapExists();
		return epicMap.containsKey(reference);
	}

	private void checkMapExists() {
		if (this.epicMap == null) {
			epicMap = new HashMap();
		}
	}

	private void cacheEpic(String reference, String attribute) {
		checkMapExists();
		epicMap.put(reference, attribute);

	}

	private IAssetType getLinkAssetType() {
		if (this.linkAssetType == null) {
			this.linkAssetType = versionOneServices.getMeta().getAssetType(LinkType);
			return linkAssetType;
		}
		return linkAssetType;
	}

	private IAssetType getExpressionAssetType() {
		if (this.expressionAssetType == null) {
			this.expressionAssetType = versionOneServices.getMeta().getAssetType(expressionType);
			return expressionAssetType;
		}
		return expressionAssetType;
	}

	private IAssetType getEpicType() {

		if (epicType == null) {
			epicType = versionOneServices.getMeta().getAssetType(EpicType);
		}
		return epicType;

	}

	private IAssetType getDefectType() {

		if (defectType == null) {
			defectType = versionOneServices.getMeta().getAssetType(DefectType);
		}
		return defectType;

	}

	private IAssetType getStoryType() {

		if (storyType == null) {
			storyType = versionOneServices.getMeta().getAssetType(StoryType);
		}
		return storyType;

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

	private IAttributeDefinition getLinkNameAttribute() {
		if (this.linkNameAttribute == null) {
			this.linkNameAttribute = getLinkAssetType().getAttributeDefinition("Name");
		}
		return linkNameAttribute;
	}

	private IAttributeDefinition getLinkURLAttribute() {

		if (this.linkURLAttribute == null) {
			this.linkURLAttribute = getLinkAssetType().getAttributeDefinition("URL");
		}

		return linkURLAttribute;
	}

	private IAttributeDefinition getLinkOnMenuAttribute() {
		if (this.linkOnMenuAttribute == null) {
			this.linkOnMenuAttribute = getLinkAssetType().getAttributeDefinition("OnMenu");
		}

		return linkOnMenuAttribute;
	}

	private IAttributeDefinition getLinkAssetAttribute() {
		if (this.linkAssetAttribute == null) {
			this.linkAssetAttribute = getLinkAssetType().getAttributeDefinition("Asset");
		}

		return linkAssetAttribute;
	}

	private IAttributeDefinition getEpicPriorityAttribute() {
		if (this.epicPriorityAttribute == null) {
			this.epicPriorityAttribute = getEpicType().getAttributeDefinition("Priority");
		}

		return epicPriorityAttribute;
	}

	private IAttributeDefinition getEpicReferenceAttribute() {
		if (this.epicReferenceAttribute == null) {
			this.epicReferenceAttribute = getEpicType().getAttributeDefinition("Reference");
		}

		return epicReferenceAttribute;
	}

	private IAttributeDefinition getEpicEpicNumberAttribute() {
		if (this.epicEpicNumberAttribute == null) {
			this.epicEpicNumberAttribute = getEpicType().getAttributeDefinition("Number");
		}

		return epicEpicNumberAttribute;
	}

	private IAttributeDefinition getEpicNameAttribute() {
		if (this.epicNameAttribute == null) {
			this.epicNameAttribute = getEpicType().getAttributeDefinition("Name");
		}

		return epicNameAttribute;
	}

	private IAttributeDefinition getEpicDescriptionAttribute() {
		if (this.epicDescriptionAttribute == null) {
			this.epicDescriptionAttribute = getEpicType().getAttributeDefinition("Description");
		}

		return epicDescriptionAttribute;
	}

	private IAttributeDefinition getEpicResolutionAttribute() {
		if (this.epicResolutionAttribute == null) {
			this.epicResolutionAttribute = getEpicType().getAttributeDefinition("Resolution");
		}

		return epicResolutionAttribute;
	}

	private IAttributeDefinition getEpicFixedInBuildAttribute() {
		if (this.epicFixedInBuildAttribute == null) {
			this.epicFixedInBuildAttribute = getEpicType().getAttributeDefinition("FixedInBuild");
		}

		return epicFixedInBuildAttribute;
	}

	private IAttributeDefinition getEpicSourceAttribute() {
		if (this.epicSourceAttribute == null) {
			this.epicSourceAttribute = getEpicType().getAttributeDefinition("Source");
		}

		return epicSourceAttribute;
	}

	private IAttributeDefinition getEpicFoundByAttribute() {
		if (this.epicFoundByAttribute == null) {
			this.epicFoundByAttribute = getEpicType().getAttributeDefinition("FoundBy");
		}

		return epicFoundByAttribute;
	}

	private IAttributeDefinition getEpicSuperAttribute() {
		if (this.epicSuperAttribute == null) {
			this.epicSuperAttribute = getEpicType().getAttributeDefinition("Super");
		}

		return epicSuperAttribute;
	}

	private IAttributeDefinition getEpicCustom_JiraepicCreationDate() {
		if (this.epicCustom_JiraepicCreationDate == null) {
			this.epicCustom_JiraepicCreationDate = getEpicType().getAttributeDefinition("Custom_JiraCreateDate2");
		}

		return epicCustom_JiraepicCreationDate;
	}

	// Defect Attribute Definitions

	private IAttributeDefinition getDefectPriorityAttribute() {
		if (this.defectPriorityAttribute == null) {
			this.defectPriorityAttribute = getDefectType().getAttributeDefinition("Priority");
		}

		return defectPriorityAttribute;
	}

	private IAttributeDefinition getDefectReferenceAttribute() {
		if (this.defectReferenceAttribute == null) {
			this.defectReferenceAttribute = getDefectType().getAttributeDefinition("Reference");
		}

		return defectReferenceAttribute;
	}

	private IAttributeDefinition getDefectParentAttribute() {
		if (this.defectParentAttribute == null) {
			this.defectParentAttribute = getDefectType().getAttributeDefinition("Parent");
		}

		return defectParentAttribute;
	}

	private IAttributeDefinition getDefectEpicNumberAttribute() {
		if (this.defectEpicNumberAttribute == null) {
			this.defectEpicNumberAttribute = getDefectType().getAttributeDefinition("Number");
		}

		return defectEpicNumberAttribute;
	}

	private IAttributeDefinition getDefectNameAttribute() {
		if (this.defectNameAttribute == null) {
			this.defectNameAttribute = getDefectType().getAttributeDefinition("Name");
		}

		return defectNameAttribute;
	}

	private IAttributeDefinition getDefectDescriptionAttribute() {
		if (this.defectDescriptionAttribute == null) {
			this.defectDescriptionAttribute = getDefectType().getAttributeDefinition("Description");
		}

		return defectDescriptionAttribute;
	}

	private IAttributeDefinition getDefectResolutionAttribute() {
		if (this.defectResolutionAttribute == null) {
			this.defectResolutionAttribute = getDefectType().getAttributeDefinition("Resolution");
		}

		return defectResolutionAttribute;
	}

	private IAttributeDefinition getDefectFixedInBuildAttribute() {
		if (this.defectFixedInBuildAttribute == null) {
			this.defectFixedInBuildAttribute = getDefectType().getAttributeDefinition("FixedInBuild");
		}

		return defectFixedInBuildAttribute;
	}

	private IAttributeDefinition getDefectSourceAttribute() {
		if (this.defectSourceAttribute == null) {
			this.defectSourceAttribute = getDefectType().getAttributeDefinition("Source");
		}

		return defectSourceAttribute;
	}

	private IAttributeDefinition getDefectTypeAttribute() {
		if (this.defectTypeAttribute == null) {
			this.defectTypeAttribute = getDefectType().getAttributeDefinition("Type");
		}

		return defectTypeAttribute;
	}

	private IAttributeDefinition getDefectFoundByAttribute() {
		if (this.defectFoundByAttribute == null) {
			this.defectFoundByAttribute = getDefectType().getAttributeDefinition("FoundBy");
		}

		return defectFoundByAttribute;
	}

	private IAttributeDefinition getDefectSuperAttribute() {
		if (this.defectSuperAttribute == null) {
			this.defectSuperAttribute = getDefectType().getAttributeDefinition("Super");
		}

		return defectSuperAttribute;
	}

	private IAttributeDefinition getDefectEstimateAttribute() {
		if (this.defectEstimateAttribute == null) {
			this.defectEstimateAttribute = getDefectType().getAttributeDefinition("Estimate");
		}

		return defectEstimateAttribute;
	}

	private IAttributeDefinition getDefectCustom_JiraDefectCreationDate() {
		if (this.defectCustom_JiraDefectCreationDate == null) {
			this.defectCustom_JiraDefectCreationDate = getDefectType().getAttributeDefinition("Custom_JiraCreateDate");
		}

		return defectCustom_JiraDefectCreationDate;
	}

	private IAttributeDefinition getDefectCustom_ITSMReference() {
		if (this.defectCustom_ITSMReference == null) {
			this.defectCustom_ITSMReference = getDefectType().getAttributeDefinition("Custom_ITSMReference");
		}

		return defectCustom_ITSMReference;
	}

	// Story Attribute Definitions

	private IAttributeDefinition getStoryPriorityAttribute() {
		if (this.storyPriorityAttribute == null) {
			this.storyPriorityAttribute = getStoryType().getAttributeDefinition("Priority");
		}

		return storyPriorityAttribute;
	}

	private IAttributeDefinition getStoryReferenceAttribute() {
		if (this.storyReferenceAttribute == null) {
			this.storyReferenceAttribute = getStoryType().getAttributeDefinition("Reference");
		}

		return storyReferenceAttribute;
	}

	private IAttributeDefinition getStoryParentAttribute() {
		if (this.storyParentAttribute == null) {
			this.storyParentAttribute = getStoryType().getAttributeDefinition("Parent");
		}

		return storyParentAttribute;
	}

	private IAttributeDefinition getStoryEpicNumberAttribute() {
		if (this.storyEpicNumberAttribute == null) {
			this.storyEpicNumberAttribute = getStoryType().getAttributeDefinition("Number");
		}

		return storyEpicNumberAttribute;
	}

	private IAttributeDefinition getStoryNameAttribute() {
		if (this.storyNameAttribute == null) {
			this.storyNameAttribute = getStoryType().getAttributeDefinition("Name");
		}

		return storyNameAttribute;
	}

	private IAttributeDefinition getStoryDescriptionAttribute() {
		if (this.storyDescriptionAttribute == null) {
			this.storyDescriptionAttribute = getStoryType().getAttributeDefinition("Description");
		}

		return storyDescriptionAttribute;
	}

	private IAttributeDefinition getStoryEstimateAttribute() {
		if (this.storyEstimateAttribute == null) {
			this.storyEstimateAttribute = getStoryType().getAttributeDefinition("Estimate");
		}

		return storyEstimateAttribute;
	}

	// private IAttributeDefinition getStoryResolutionAttribute() {
	// if (this.storyResolutionAttribute == null) {
	// this.storyResolutionAttribute =
	// getStoryType().getAttributeDefinition("Resolution");
	// }
	//
	// return storyResolutionAttribute;
	// }

	// private IAttributeDefinition getStoryFixedInBuildAttribute() {
	// if (this.storyFixedInBuildAttribute == null) {
	// this.storyFixedInBuildAttribute =
	// getStoryType().getAttributeDefinition("FixedInBuild");
	// }
	//
	// return storyFixedInBuildAttribute;
	// }

	private IAttributeDefinition getStorySourceAttribute() {
		if (this.storySourceAttribute == null) {
			this.storySourceAttribute = getStoryType().getAttributeDefinition("Source");
		}

		return storySourceAttribute;
	}

	private IAttributeDefinition getStoryTypeAttribute() {
		if (this.storyTypeAttribute == null) {
			this.storyTypeAttribute = getStoryType().getAttributeDefinition("Category");
		}

		return storyTypeAttribute;
	}

	// private IAttributeDefinition getStoryFoundByAttribute() {
	// if (this.storyFoundByAttribute == null) {
	// this.storyFoundByAttribute =
	// getStoryType().getAttributeDefinition("FoundBy");
	// }
	//
	// return storyFoundByAttribute;
	// }

	private IAttributeDefinition getStorySuperAttribute() {
		if (this.storySuperAttribute == null) {
			this.storySuperAttribute = getStoryType().getAttributeDefinition("Super");
		}

		return storySuperAttribute;
	}

	private IAttributeDefinition getStoryCustom_JiraStoryCreationDate() {
		if (this.storyCustom_JirastoryCreationDate == null) {
			this.storyCustom_JirastoryCreationDate = getStoryType().getAttributeDefinition("Custom_JiraCreateDate3");
		}

		return storyCustom_JirastoryCreationDate;
	}

	private IAttributeDefinition getStoryCustom_ITSMReference() {
		if (this.storyCustom_ITSMReference == null) {
			this.storyCustom_ITSMReference = getStoryType().getAttributeDefinition("Custom_ITSMReference2");
		}

		return storyCustom_ITSMReference;
	}

	public boolean checkWorkItemExists(String key) {
		Query query = new Query(versionOneServices.getMeta().getAssetType(workItemType));

		query.getSelection()
				.add(versionOneServices.getMeta().getAssetType(workItemType).getAttributeDefinition("Reference"));
		query.getSelection()
				.add(versionOneServices.getMeta().getAssetType(workItemType).getAttributeDefinition("Name"));

		query.getSelection()
				.add(versionOneServices.getMeta().getAssetType(workItemType).getAttributeDefinition("Reference"));
		query.getSelection()
				.add(versionOneServices.getMeta().getAssetType(workItemType).getAttributeDefinition("Name"));
		FilterTerm referenceTerm = new FilterTerm(
				versionOneServices.getMeta().getAssetType(workItemType).getAttributeDefinition("Reference"));
		referenceTerm.equal(key);
		query.setFilter(referenceTerm);
		QueryResult result = null;
		try {
			result = versionOneServices.retrieve(query);
		} catch (ConnectionException | APIException | OidException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (result.getTotalAvaliable() > 0) {
			System.out.println("Story already created before this run : " + key);

			return true;
		} else
			return false;

	}

}
