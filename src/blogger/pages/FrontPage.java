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

public class FrontPage extends HttpMethodDispatchHandler {
	private final SqlTable<Post> posts;

	public FrontPage(SqlTable<Post> posts) {
		super(HttpMethodDispatchHandler.ALLOW_GET);
		this.posts = posts;
	}

	@Override
	public void get(HttpRequest request, HttpResponse response, HttpContext context)
			throws HttpException, IOException {
		String uri = request.getRequestLine().getUri();
		try {
			List<NameValuePair> parameters = new URIBuilder(uri).getQueryParams();
			ByteArrayOutputStream ous = new ByteArrayOutputStream();
			PrintWriter writer = new PrintWriter(ous);
			writePage(writer);
			writer.flush();
			response.setStatusCode(HttpStatus.SC_OK);
			response.setEntity(new ByteArrayEntity(ous.toByteArray(), ContentType.TEXT_HTML));
		} catch(URISyntaxException e) {
			throw new HttpException("Invalid URI",e);
		}
	}

	private void writePage(PrintWriter writer) {
		writer.println("<!DOCTYPE html>");
		writer.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" dir=\"ltr\" lang=\"en-US\">");
		writeHeader(writer);
		writeBody(writer);
		writer.println("</html>");
	}

	public void writeHeader(PrintWriter writer) {
		writer.println("<head>");
		writer.println("<title>Blogger Test</title>");
		writer.println("<link href=\"css/style.css\" rel=\"stylesheet\" type=\"text/css\">");
		writer.println("</head>");

	}

	public void writeBody(PrintWriter writer) {
		writer.println("<body>");
		writer.println("<div id='header'>");
		writer.println("<h1 class='blogtitle'>Whiley</h1>");
		writer.println("<p>A Programming Language with Extended Static Checking</p>");
		writer.println("</div>");
		writer.println("<div id='container'>");
		writer.println("<div id='content'>");
		for(Post post : posts.select()) {
			writePost(post,writer);
		}
		writer.println("</div></div></body>");
	}

	public void writePost(Post post, PrintWriter writer) {
		writer.println("<div class='post'>");
		writer.println("<div class='post-headline'>");
		writer.println(post.title());
		writer.println("</div><div class='post-byline'>");
		writer.println("By Dave, ");
		writer.println("on " + post.date());
		writer.println("</div><div class='post-body'>");
		writer.println("The body");
		writer.println("</div><div class='post-footer'>");
		writer.println("The footer");
		writer.println("</div></div>");
	}
}
