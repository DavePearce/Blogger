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
import jwebkit.sql.SqlQuery;
import jwebkit.sql.SqlTable;
import jwebkit.sql.SqlValue;

public class PostPages extends HttpMethodDispatchHandler {
	private final SqlTable<Post> posts;

	public PostPages(SqlTable<Post> posts) {
		super(HttpMethodDispatchHandler.ALLOW_GET);
		this.posts = posts;
	}

	@Override
	public void get(HttpRequest request, HttpResponse response, HttpContext context)
			throws HttpException, IOException {
		String uri = request.getRequestLine().getUri();
		Post post = getPost(uri);
		try {
			//
			List<NameValuePair> parameters = new URIBuilder(uri).getQueryParams();
			ByteArrayOutputStream ous = new ByteArrayOutputStream();
			PrintWriter writer = new PrintWriter(ous);
			writePage(writer,post);
			writer.flush();
			response.setStatusCode(HttpStatus.SC_OK);
			response.setEntity(new ByteArrayEntity(ous.toByteArray(), ContentType.TEXT_HTML));
		} catch(URISyntaxException e) {
			throw new HttpException("Invalid URI",e);
		}
	}


	private void writePage(PrintWriter writer, Post post) {
		writer.println("<!DOCTYPE html>");
		writer.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" dir=\"ltr\" lang=\"en-US\">");
		writeHeader(writer);
		writeBody(writer, post);
		writer.println("</html>");
	}

	public void writeHeader(PrintWriter writer) {
		writer.println("<head>");
		writer.println("<title>Blogger Test</title>");
		writer.println("<link href=\"css/style.css\" rel=\"stylesheet\" type=\"text/css\">");
		writer.println("</head>");
	}

	public void writeBody(PrintWriter writer, Post p) {
		writer.println("<body>");
		writer.println("<div id='header'>");
		writer.println("<h1 class='blogtitle'>Whiley</h1>");
		writer.println("<p>A Programming Language with Extended Static Checking</p>");
		writer.println("</div>");
		writer.println("<div id='container'>");
		writer.println("<div id='content'>");
		writePostTitle(writer,p);
		writePostBody(writer,p);
		writer.println("</div></div></body>");
	}

	protected void writePostTitle(PrintWriter writer, Post p) {
		writer.print("<h1>");
		writer.print(p.title());
		writer.print("</h1>");
	}

	protected void writePostBody(PrintWriter writer, Post p) {
		writer.print(p.body());
	}

	private Post getPost(String uri) {
		String link = uri.substring(1);
		for (Post p : posts.select().whereEqual("link", new SqlValue.Text(link))) {
			return p;
		}
		return null;
	}
}
