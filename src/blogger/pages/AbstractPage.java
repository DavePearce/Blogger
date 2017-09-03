package blogger.pages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HttpContext;

import blogger.Main.Post;
import jwebkit.http.HttpMethodDispatchHandler;
import jwebkit.sql.SqlTable;

public abstract class AbstractPage extends HttpMethodDispatchHandler {

	public AbstractPage() {
		super(HttpMethodDispatchHandler.ALLOW_GET);
	}

	@Override
	public void get(HttpRequest request, HttpResponse response, HttpContext context)
			throws HttpException, IOException {
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(ous);
		writePage(writer,request);
		writer.flush();
		response.setStatusCode(HttpStatus.SC_OK);
		response.setEntity(new ByteArrayEntity(ous.toByteArray(), ContentType.TEXT_HTML));
	}

	private void writePage(PrintWriter writer, HttpRequest request) {
		writer.println("<!DOCTYPE html>");
		writer.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" dir=\"ltr\" lang=\"en-US\">");
		writeHeader(writer,request);
		writeBody(writer,request);
		writer.println("</html>");
	}

	public void writeHeader(PrintWriter writer, HttpRequest request) {
		writer.println("<head>");
		writer.println("<title>Blogger Test</title>");
		writer.println("<link href=\"/css/style.css\" rel=\"stylesheet\" type=\"text/css\">");
		writer.println("</head>");
	}

	public void writeBody(PrintWriter writer, HttpRequest request) {
		writer.println("<body>");
		writer.println("<div id='container'>");
		writePageHeader(writer,request);
		writePageOutline(writer,request);
		writePageFooter(writer,request);

		writer.println("</div></body>");
	}

	public void writePageHeader(PrintWriter writer, HttpRequest request) {
		writer.println("<div id='header'>");
		writer.println("<h1 class='blogtitle'>Whiley</h1>");
		writer.println("<p>A Programming Language with Extended Static Checking</p>");
		writer.println("</div>");
	}
	public void writePageOutline(PrintWriter writer, HttpRequest request) {
		writer.println("<table id='layout'>");
		writer.println("<tr>");
		writeLeftSidebar(writer,request);
		writePageContent(writer,request);
		writeRightSidebar(writer,request);
		writer.println("</tr>");
		writer.println("</table>");
	}
	public void writeLeftSidebar(PrintWriter writer, HttpRequest request) {
		writer.println("<td id='left-sidebar'>");
		writeAboutWidget(writer);
		writeContributeWidget(writer);
		writer.println("</td>");
	}

	public void writeAboutWidget(PrintWriter writer) {
		writeBeginSidebar("ABOUT",writer);
		writeSidebarItem("Play","http://whileylabs.com",writer);
		writeSidebarItem("People","/about/people/",writer);
		writeSidebarItem("Overview","/about/overview/",writer);
		writeSidebarItem("Documentation","/about/documentation/",writer);
		writeSidebarItem("Getting Started","/about/getting-started/",writer);
		writeSidebarItem("Publications","/about/publications/",writer);
		writeSidebarItem("Roadmap","/about/roadmap/",writer);
		writeSidebarItem("FAQ","/about/faq/",writer);
		writeEndSidebar(writer);
	}
	public void writeContributeWidget(PrintWriter writer) {
		writeBeginSidebar("CONTRIBUTE",writer);
		writeSidebarItem("Github","http://github.com/Whiley",writer);
		writeSidebarItem("Openhub","https://www.openhub.net/orgs/Whiley",writer);
		writeSidebarItem("Compiler API","/docs/api/index.html",writer);
		writeSidebarItem("Forum","https://groups.google.com/forum/#!forum/whiley-discuss",writer);
		writeEndSidebar(writer);
	}

	public void writePageContent(PrintWriter writer, HttpRequest request) {
		writer.println("<td id='content'>");
		writeContent(writer,request);
		writer.println("</td>");
	}
	public void writeRightSidebar(PrintWriter writer, HttpRequest request) {
		writer.println("<td id='right-sidebar'>");
		writer.println("right sidebar");
		writer.println("</td>");
	}
	public void writePageFooter(PrintWriter writer, HttpRequest request) {
		writer.println("<div id='footer'>");
		writer.println("</div>");
	}
	public void writeBeginSidebar(String title, PrintWriter writer) {
		writer.println("<div class='sidebar-title'>" + title + "</div>");
		writer.println("<ul>");
	}
	public void writeSidebarItem(String title, String url, PrintWriter writer) {
		writer.println("<li>");
		writer.println("<a class='sidebar-title' href='" + url + "'>" + title + "</a>");
		writer.println("</li>");
	}
	public void writeEndSidebar(PrintWriter writer) {
		writer.println("</ul>");
		writer.println("<hr class='sidebar-separator'/>");
	}
	public abstract void writeContent(PrintWriter writer, HttpRequest request);
}
