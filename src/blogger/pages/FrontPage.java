package blogger.pages;

import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import blogger.core.Posts;
import blogger.core.Posts.Post;
import jwebkit.sql.SqlQuery;
import jwebkit.sql.SqlTable;

public class FrontPage extends AbstractPage {
	private final int POSTS_PER_PAGE = 10;
	//
	private final Posts posts;

	public FrontPage(Posts posts) {
		this.posts = posts;
	}

	@Override
	public void writeContent(PrintWriter writer, HttpRequest request) {
		// Collect all the results into an array list.
		ArrayList<Post> results = posts.select()
				.orderByDesc(posts.getColumn("datetime"))
				.collect(new ArrayList<Post>());
		//
		System.out.println("RESULTS: " + results.size());
		// Determine page number and other attributes
		int page = getPage(request);
		int numPages = results.size() / POSTS_PER_PAGE;
		int start = page * POSTS_PER_PAGE;
		int end = start + POSTS_PER_PAGE;
		// Write post range selected
		for(int i=start;i<end;++i) {
			if(i < results.size()) {
				writePost(results.get(i), writer);
			}
		}
		// Print out links for next pages
		if(page > 0) {
			writer.print("<a href=\"?page=" + (page-1) + "\">");
			writer.print("<< previous");
			writer.print("</a> ");
		}
		if((page+1) < numPages) {
			writer.print("<a href=\"?page=" + (page+1) + "\">");
			writer.print("more >>");
			writer.print("</a> ");
		}
	}

	public void writePost(Post post, PrintWriter writer) {
		writer.println("<div class='post'>");
		LocalDateTime date = post.date();
		String link = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/" + post.link();
		writer.println("<div class='post-headline'><a href=\"/" + link + "\">");
		writer.println(post.title());
		writer.println("</a></div><div class='post-byline'>");
		writer.println("By Dave, ");
		writer.println("on " + post.date());
		writer.println("</div><div class='post-summary'>");
		String exerpt = createExcerpt(post.body());
		writer.println(exerpt);
		writer.println("</div></div>");
	}

	public String createExcerpt(String body) {
		String stripped = body.replaceAll("\\<.*?\\>", "");
		int length = Math.min(300, stripped.length());
		return stripped.substring(0,length) + "...";
	}

	public int getPage(HttpRequest request) {
		String uri = request.getRequestLine().getUri();
		try {
			List<NameValuePair> parameters = new URIBuilder(uri).getQueryParams();
			for(int i=0;i!=parameters.size();++i) {
				NameValuePair nvp = parameters.get(i);
				if(nvp.getName().equals("page")) {
					return Integer.parseInt(nvp.getValue());
				}
			}
		} catch(URISyntaxException e) {
		}
		// This would seem to be a sensible default
		return 0;
	}
}
