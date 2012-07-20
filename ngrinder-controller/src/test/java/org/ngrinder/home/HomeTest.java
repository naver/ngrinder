package org.ngrinder.home;

import java.io.IOException;
import java.util.List;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.junit.Test;

public class HomeTest {
	@Test
	public void testHome() throws IOException {
		IssueService service = new IssueService();
		RepositoryId repo = new RepositoryId("nhnopensource", "ngrinder");
		
		List<Issue> issues = service.getIssues(repo, null);
		for (Issue issue : issues) {
			System.out.println(issue.getTitle());
		}
	}
}
