package ikube.index.handler.internet.crawler;

import ikube.model.Url;

/**
 * This class runs the JavaScript on the page. If there is a JavaScript exception thin the error will be collected and can be displayed in
 * the output.
 * 
 * @author Michael Couck
 * @since 25.09.10
 * @version 01.00
 */
public class UrlScriptHandler extends UrlHandler<Url> {

	/**
	 * This method will execute the JavaScript in the page.
	 * 
	 * @See {@link IUrlHandler#handle(Url)}
	 */
	public void handle(final Url url) {
		// TODO - implement me. We could use Rhino for executing the JavaScript
		// or some other library for the execution, perhaps index the result too.
	}

	@Override
	public void run() {
	}

}