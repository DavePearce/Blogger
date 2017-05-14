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

public class PostPages extends AbstractPage {
	private final SqlTable<Post> posts;

	public PostPages(SqlTable<Post> posts) {
		this.posts = posts;
	}

	@Override
	public void writeContent(PrintWriter writer, HttpRequest request) {
		String uri = request.getRequestLine().getUri();
		Post post = getPost(uri);
		writePostTitle(writer,post);
		writePostBody(writer,post);
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
