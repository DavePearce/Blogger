package blogger.pages;

import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import blogger.Main.Post;
import jwebkit.sql.SqlQuery;
import jwebkit.sql.SqlTable;

public class FrontPage extends AbstractPage {
	private final int POSTS_PER_PAGE = 10;
	//
	private final SqlTable<Post> posts;

	public FrontPage(SqlTable<Post> posts) {
		this.posts = posts;
	}

	@Override
	public void writeContent(PrintWriter writer, HttpRequest request) {
		Iterator<Post> query = posts.select().iterator();
		int page = getPage(request);
		int start = page * POSTS_PER_PAGE;
		int i = 0;
		while(query.hasNext() && i < (start+POSTS_PER_PAGE)) {
			Post post = query.next();
			if(i >= start) {
				writePost(post,writer);
			}
			i = i + 1;
		}
	}

	public void writePost(Post post, PrintWriter writer) {
		writer.println("<div class='post'>");
		writer.println("<div class='post-headline'><a href=\"/" + post.link() + "\">");
		writer.println(post.title());
		writer.println("</a></div><div class='post-byline'>");
		writer.println("By Dave, ");
		writer.println("on " + post.date());
		writer.println("</div><div class='post-body'>");
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
