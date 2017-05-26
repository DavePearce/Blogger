package blogger.pages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
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
		writer.println("<link href=\"css/style.css\" rel=\"stylesheet\" type=\"text/css\">");
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
		writer.println("left sidebar");
		writer.println("</td>");
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
		writer.println("This is the footer");
		writer.println("</div>");
	}
	public abstract void writeContent(PrintWriter writer, HttpRequest request);
}
