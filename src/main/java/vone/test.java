package vone;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
import com.versionone.Oid;
import com.versionone.apiclient.Asset;
import com.versionone.apiclient.Query;
import com.versionone.apiclient.Services;
import com.versionone.apiclient.V1Connector;
import com.versionone.apiclient.exceptions.APIException;
import com.versionone.apiclient.exceptions.ConnectionException;
import com.versionone.apiclient.exceptions.OidException;
import com.versionone.apiclient.exceptions.V1Exception;
import com.versionone.apiclient.interfaces.IAssetType;
import com.versionone.apiclient.interfaces.IAttributeDefinition;
import com.versionone.apiclient.interfaces.IServices;
import com.versionone.apiclient.services.QueryResult;
import com.versionone.utils.Version;

public class test {
	public static void main(String[] args) throws Exception, V1Exception {
        // Prints "Hello, World" to the terminal window.
        System.out.println("Hello, World");
       
			V1Connector connector = V1Connector
				    .withInstanceUrl("https://www51.v1host.com/ShortcutsSoftwareLimited54/")
				    .withUserAgentHeader("AppName", "1.0")
				    .withAccessToken("1.c663I0LtO3oL6pwBs0enw1rfeos=")
				    .build();
			

		    		   IServices services = new Services(connector);
		    		   
				       IAttributeDefinition nameAttribute;

				       
				       misc(services);
				       
				       
				       createAsset(services);
				       
				       mysql();
				       
				    }

	private static void createAsset(IServices services)
			throws OidException, V1Exception, APIException, ConnectionException {
		IAttributeDefinition nameAttribute;
		Oid projectId = services.getOid("Scope:1541");
		   IAssetType storyType = services.getMeta().getAssetType("Epic");
		   Asset newStory = services.createNew(storyType, projectId);
		    nameAttribute = storyType.getAttributeDefinition("Name");
		   newStory.setAttributeValue(nameAttribute, "My New Story");
		   services.save(newStory);

		   System.out.println(newStory.getOid().getToken());
		   System.out.println(newStory.getAttribute(storyType.getAttributeDefinition("Scope")).getValue());
		   System.out.println(newStory.getAttribute(nameAttribute).getValue());

		   /***** OUTPUT *****
		   Story:7617:9243
		   Scope:0
		   My New Story
		   ******************/
	}

	private static void misc(IServices services) throws OidException, ConnectionException, APIException {
		System.out.println("Hello, World");
		   Oid memberId = services.getOid("Scope:1535");
		   Query query = new Query(memberId);
		   QueryResult result = services.retrieve(query);
		   Asset member = result.getAssets()[0];

		   System.out.println(member.getOid().getToken());

		   /***** OUTPUT *****
		   Member:20
		   ******************/
		   
		 memberId = services.getOid("Member:20");
		    query = new Query(memberId);
		   IAttributeDefinition nameAttribute = services.getMeta().getAttributeDefinition("Member.Name");
		   IAttributeDefinition emailAttribute = services.getMeta().getAttributeDefinition("Member.Email");
		   query.getSelection().add(nameAttribute);
		   query.getSelection().add(emailAttribute);
		    result = services.retrieve(query);
		    member = result.getAssets()[0];

		   System.out.println(member.getOid().getToken());
		   System.out.println(member.getAttribute(nameAttribute).getValue());
		   System.out.println(member.getAttribute(emailAttribute).getValue());

		   /***** OUTPUT *****
		   Member:20
		   Administrator
		   admin@company.com
		   ******************/
	}

	private static void mysql() {
		Connection con = null;
		    Statement st = null;
		    ResultSet rs = null;

		    String url = "jdbc:mysql://localhost:3306/mydb";
		    String user = "root";
		    String password = "";

		    try {
		        
		        con = (Connection) DriverManager.getConnection(url, user, password);
		        st = (Statement) con.createStatement();
		        rs = st.executeQuery("SELECT * from jira_portfolio_hierarchy_flat");

		        while (rs.next()) {
		            
		            System.out.print(rs.getString(1));
		            System.out.print(rs.getString(2));
		            System.out.print(rs.getString(3));
		            System.out.print(rs.getString(4));
		            System.out.print(rs.getString(5));
		            System.out.print(rs.getString(6));
		            System.out.print(rs.getString(7));
		            System.out.print(rs.getString(8));
		            System.out.print(rs.getString(9));
		            System.out.println(rs.getString(10));

		        }

		    } catch (SQLException ex) {
		    
		        Logger lgr = Logger.getLogger(Version.class.getName());
		        lgr.log(Level.SEVERE, ex.getMessage(), ex);

		    } finally {
		        
		        try {
		            
		            if (rs != null) {
		                rs.close();
		            }
		            
		            if (st != null) {
		                st.close();
		            }
		            
		            if (con != null) {
		                con.close();
		            }

		        } catch (SQLException ex) {
		            
		            Logger lgr = Logger.getLogger(Version.class.getName());
		            lgr.log(Level.WARNING, ex.getMessage(), ex);
		        }
		    }
	}
				       
				    		   



}
