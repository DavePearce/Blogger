package blogger.pages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Arrays;
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
		writer.print("<div class=\"post-title\">");
		writer.print(p.title());
		writer.print("</div>");
	}

	protected void writePostBody(PrintWriter writer, Post p) {
		writer.print("<div class=\"post-body\">");
		String body = apply_filters(p.body());
		writer.print(body);
		writer.print("</div>");
	}

	private String apply_filters(String body) {
		String[] lines = body.split("\r\n");
		StringBuilder builder = new StringBuilder();
		boolean inCode = false;
		for(int i=0;i!=lines.length;++i) {
			String line = lines[i];
			// Strip out Unicode SPACE.
			line = line.replace("\u00A0"," ");
			//
			if(line.equals(START_TAG)) {
				builder.append("<div class='whiley-code'>");
				inCode=true;
			} else if(line.equals(END_TAG)) {
				builder.append("</div>");
				inCode=false;
			} else if(inCode) {
				for (int j = 0; j != keywords.length; ++j) {
					String keyword = keywords[j];
					line = line.replaceAll(keyword, "<strong>" + keyword + "</strong>");
				}
				builder.append(line + "\n");
			} else if(!inCode) {
				builder.append("<p>");
				builder.append(line);
				builder.append("</p>");
			}
		}
		return builder.toString();
	}

	private static final String[] keywords = { "case", "catch", "continue", "debug", "default", "do", "else", "ensures",
			"export", "false", "fail", "finite", "for", "function", "if", "import", "in", "int", "is", "method",
			"native", "new", "no", "null", "package", "private", "protected", "public", "requires", "return", "skip",
			"some", "switch", "throw", "this", "throws", "total", "true", "type", "void", "where", "while" };


	private static final String START_TAG = "[whiley]";
	private static final String END_TAG = "[/whiley]";


	private Post getPost(String uri) {
		String[] splits = uri.substring(1).split("/");
		String link = splits[splits.length-1];
		for (Post p : posts.select().whereEqual("link", new SqlValue.Text(link))) {
			return p;
		}
		return null;
	}
}
