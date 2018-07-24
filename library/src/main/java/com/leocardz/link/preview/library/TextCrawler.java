package com.leocardz.link.preview.library;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class TextCrawler {

	public static final int ALL = -1;
	public static final int NONE = -2;

	private final String HTTP_PROTOCOL = "http://";
	private final String HTTPS_PROTOCOL = "https://";

	private LinkPreviewCallback callback;
	private String currentUrl;

	private AsyncTask getCodeTask;

	public TextCrawler() {
	}

	public void makePreview(String currentUrl,LinkPreviewCallback callback, String url) {
		makePreview(currentUrl,callback, url, ALL);
	}

	public void makePreview(String currentUrl,LinkPreviewCallback callback, String url,
							int imageQuantity) {
		this.currentUrl=currentUrl;
		this.callback = callback;
		cancel();
		getCodeTask = createPreviewGenerator(imageQuantity).execute(url);
	}

	protected GetCode createPreviewGenerator(int imageQuantity) {
		return new GetCode(imageQuantity);
	}

	public void cancel(){
		if(getCodeTask != null){
			getCodeTask.cancel(true);
		}
	}


	/** Get html code */
	public class GetCode extends AsyncTask<String, Void, Void> {

		private SourceContent sourceContent = new SourceContent();
		private int imageQuantity;
		private String urls;

		public GetCode(int imageQuantity) {
			this.imageQuantity = imageQuantity;
		}

		@Override
		protected void onPreExecute() {
			if (callback != null) {
				callback.onPre();
			}
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			if (callback != null) {
				callback.onPos(sourceContent, isNull());
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected Void doInBackground(String... params) {
			// Don't forget the http:// or https://
			urls = (params[0]);

//			if (urls.size() > 0)
			sourceContent
					.setFinalUrl(unshortenUrl(extendedTrim(urls)));
//			else
//				sourceContent.setFinalUrl("");

			if (!sourceContent.getFinalUrl().equals("")) {



				if(sourceContent.getFinalUrl().contains("www.target"))
				{
					URL url = null;
					URLConnection urlConnection = null;
					BufferedReader bufferedReader = null;
					String inputLine;
					String strtempHtml = "";
					try {
						url = new URL(sourceContent.getFinalUrl());
						urlConnection = url.openConnection();
						urlConnection.setRequestProperty("Content-type", "text/html");
						urlConnection.setRequestProperty("Accept", "text/html, application/html");
						bufferedReader = new BufferedReader(new InputStreamReader(
								urlConnection.getInputStream()));
						while ((inputLine = bufferedReader.readLine()) != null) {

							strtempHtml = strtempHtml+inputLine;

						}
						bufferedReader.close();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					HashMap<String, String> metaTags = getMetaTags(strtempHtml);
					sourceContent.setMetaTags(metaTags);
					sourceContent.setFinalUrl(metaTags.get("url"));
					sourceContent.setImage(metaTags.get("image"));
					sourceContent.setTitle(metaTags.get("title"));
					sourceContent.setDescription(metaTags.get("description"));
					if(Character.isDigit(sourceContent.getFinalUrl().charAt(sourceContent.getFinalUrl().length()-1)))
					{
						sourceContent.setPrice(getPriceForTargetWebsite(strtempHtml));

					}else
					{
						sourceContent.setPrice("");
					}

                    if(!sourceContent.getPrice().equals(""))
					{
						sourceContent.setSuccess(true);
					}else
					{
						sourceContent.setSuccess(false);
					}






				}else
				{
					try {
						Document doc = getDocument();
						sourceContent.setHtmlCode(extendedTrim(doc.toString()));
						HashMap<String, String> metaTags = getMetaTags(sourceContent
								.getHtmlCode());
						sourceContent.setMetaTags(metaTags);
						sourceContent.setTitle(metaTags.get("title"));
						sourceContent.setDescription(metaTags
								.get("description"));
						sourceContent.setPrice(getPriceValue(doc));

						if (sourceContent.getTitle().equals("")) {
							String matchTitle = Regex.pregMatch(
									sourceContent.getHtmlCode(),
									Regex.TITLE_PATTERN, 2);

							if (!matchTitle.equals(""))
								sourceContent.setTitle(htmlDecode(matchTitle));
						}
						if (sourceContent.getDescription().equals(""))
							sourceContent
									.setDescription(crawlCode(sourceContent
											.getHtmlCode()));

						sourceContent.setDescription(sourceContent
								.getDescription().replaceAll(
										Regex.SCRIPT_PATTERN, ""));

//						if (imageQuantity != NONE) {

						sourceContent.setImage(getImages(doc));

						//}
						if(sourceContent.getPrice().equals(""))
						{
							String matchPrice=Regex.pregMatch(sourceContent.getHtmlCode(),Regex.PRICE_PATTERN,1)  ;
							if (!matchPrice.equals(""))
							{
								sourceContent.setPrice(htmlDecode(matchPrice));
							}
						}

						sourceContent.setSuccess(true);
					} catch (Throwable t) {
						Log.d("TimeOut",t.toString());
						sourceContent.setSuccess(false);
					}
				}



			}

			String[] finalLinkSet = sourceContent.getFinalUrl().split("&");
			sourceContent.setUrl(finalLinkSet[0]);

			sourceContent.setCannonicalUrl(cannonicalPage(sourceContent
					.getFinalUrl()));
			sourceContent.setDescription(stripTags(sourceContent
					.getDescription()));

			return null;
		}

		protected Document getDocument() throws IOException {


			return Jsoup.connect(sourceContent.getFinalUrl()).maxBodySize(0)
					.timeout(600000)
					.get();
















//
//			URL url = new URL("https://www.target.com.au/look/happy/LOOK1212166");
//			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//			DocumentBuilder db = null;
//			try {
//				db = dbf.newDocumentBuilder();
//			} catch (ParserConfigurationException e) {
//				e.printStackTrace();
//			}
//			Document doc = null;
//			try {
//				doc = (Document) db.parse(url.openStream());
//			} catch (SAXException e) {
//				e.printStackTrace();
//			}
//			return  doc;
		}

		/** Verifies if the content could not be retrieved */
		public boolean isNull() {
			return !sourceContent.isSuccess() &&
					extendedTrim(sourceContent.getHtmlCode()).equals("") &&
					!isImage(sourceContent.getFinalUrl());
		}

	}

	/** Gets content from a html tag */
	private String getTagContent(String tag, String content) {

		String pattern = "<" + tag + "(.*?)>(.*?)</" + tag + ">";
		String result = "", currentMatch = "";

		List<String> matches = Regex.pregMatchAll(content, pattern, 2);

		int matchesSize = matches.size();
		for (int i = 0; i < matchesSize; i++) {
			if(getCodeTask.isCancelled()){
				break;
			}
			currentMatch = stripTags(matches.get(i));
			if (currentMatch.length() >= 120) {
				result = extendedTrim(currentMatch);
				break;
			}
		}

		if (result.equals("")) {
			String matchFinal = Regex.pregMatch(content, pattern, 2);
			result = extendedTrim(matchFinal);
		}

		result = result.replaceAll("&nbsp;", "");

		return htmlDecode(result);
	}



	public String getImages(Document document) {

		String imageUrl = null;
		Elements metaOgImage = document.select("meta[property=og:image]" );
		if (metaOgImage!=null) {
			imageUrl = metaOgImage.attr("content");
		}

		return imageUrl;
	}


	/** Transforms from html to normal string */
	private String htmlDecode(String content) {
		return Jsoup.parse(content).text();
	}

	/** Crawls the code looking for relevant information */
	private String crawlCode(String content) {
		String result = "";
		String resultSpan = "";
		String resultParagraph = "";
		String resultDiv = "";

		resultSpan = getTagContent("span", content);
		resultParagraph = getTagContent("p", content);
		resultDiv = getTagContent("div", content);

		result = resultSpan;

		if (resultParagraph.length() > resultSpan.length()
				&& resultParagraph.length() >= resultDiv.length())
			result = resultParagraph;
		else if (resultParagraph.length() > resultSpan.length()
				&& resultParagraph.length() < resultDiv.length())
			result = resultDiv;
		else
			result = resultParagraph;

		return htmlDecode(result);
	}

	/** Returns the cannoncial url */
	private String cannonicalPage(String url) {

		String cannonical = "";
		if (url.startsWith(HTTP_PROTOCOL)) {
			url = url.substring(HTTP_PROTOCOL.length());
		} else if (url.startsWith(HTTPS_PROTOCOL)) {
			url = url.substring(HTTPS_PROTOCOL.length());
		}

		int urlLength = url.length();
		for (int i = 0; i < urlLength; i++) {
			if(getCodeTask.isCancelled()){
				break;
			}
			if (url.charAt(i) != '/')
				cannonical += url.charAt(i);
			else
				break;
		}

		return cannonical;

	}

	/** Strips the tags from an element */
	private String stripTags(String content) {
		return Jsoup.parse(content).text();
	}

	/** Verifies if the url is an image */
	private boolean isImage(String url) {
		return url.matches(Regex.IMAGE_PATTERN);
	}



	private String getPriceValue(Document document) {
		String price = "";
		String url=this.currentUrl;
		if (url != null) {

			if (url.contains("www.walmart.com")) {

				Elements elements=document.select("div[class=product-offer-price hf-BotRow]");
				Log.d("resultDatawalmart",elements.text()+ elements.toString());
				Elements mPriceElements= elements.select("div[class=prod-PriceHero]");
				Log.d("resultDatawalmart",mPriceElements.text()+ mPriceElements.toString());
				Elements mPrice = mPriceElements.select("span[class=Price-group]");
				String pricearray[] =(mPrice.text().split(" "));
				price=pricearray[0];
				Log.d("resultDatawalmart",mPrice.text()+ mPrice.toString());

			} else if (url.contains("ebay.com")) {
				Elements mPriceElements= document.select("div[class=u-flL w29 vi-price]") ;
				Log.d("resultDataebay",mPriceElements.text()+ mPriceElements.toString());
				price =mPriceElements.text();

			} else if (url.contains("www.frys.com")) {
				Elements mPriceElements= document.select("div[class=price-details-info]") ;

				Log.d("resultData",mPriceElements.text()+ mPriceElements.toString());
				Elements priceElement=mPriceElements.select("span[class=net-total net-total-price]");
				price=priceElement.text();
				Log.d("resultData1",priceElement.text()+ priceElement.toString());


			} else if (currentUrl.contains("www.target.com")) {
				Elements mPriceElements= document.select("div[styles__SidebarContainerDiv-vttgqz-4 fBTXbo]") ;
				Log.d("resultDatatarget",mPriceElements.text()+ mPriceElements.toString());
				Elements priceElement=mPriceElements.select("span[class=price price-regular]");
				price=priceElement.text();

			}



		}
		return price;
	}
	/**
	 * Returns meta tags from html code
	 */
	private HashMap<String, String> getMetaTags(String content) {

		HashMap<String, String> metaTags = new HashMap<String, String>();
		metaTags.put("url", "");
		metaTags.put("title", "");
		metaTags.put("description", "");
		metaTags.put("image", "");
		metaTags.put("price", "");

		List<String> matches = Regex.pregMatchAll(content,
				Regex.METATAG_PATTERN, 0);

		for (String match : matches) {
			if(getCodeTask.isCancelled()){
				break;
			}
			final String lowerCase = match.toLowerCase();
			if (lowerCase.contains("property=\"og:url\"")
					|| lowerCase.contains("property='og:url'")
					|| lowerCase.contains("name=\"url\"")
					|| lowerCase.contains("name='url'"))

				updateMetaTag(metaTags, "url", separeMetaTagsContent(match));
			else if (lowerCase.contains("property=\"og:title\"")
					|| lowerCase.contains("property='og:title'")
					|| lowerCase.contains("name=\"title\"")
					|| lowerCase.contains("name='title'"))
				updateMetaTag(metaTags, "title", separeMetaTagsContent(match));
			else if (lowerCase
					.contains("property=\"og:description\"")
					|| lowerCase
					.contains("property='og:description'")
					|| lowerCase.contains("name=\"description\"")
					|| lowerCase.contains("name='description'"))
				updateMetaTag(metaTags, "description", separeMetaTagsContent(match));
			else if (lowerCase.contains("property=\"og:image\"")
					|| lowerCase.contains("property='og:image'")
					|| lowerCase.contains("name=\"image\"")
					|| lowerCase.contains("name='image'")
					)
				updateMetaTag(metaTags, "image", separeMetaTagsContent(match));
		}

		return metaTags;
	}

	private void updateMetaTag(HashMap<String, String> metaTags, String url, String value) {
		if (value != null && (value.length() > 0)) {
			metaTags.put(url, value);
		}
	}

	/** Gets content from metatag */
	private String separeMetaTagsContent(String content) {
		String result = Regex.pregMatch(content, Regex.METATAG_CONTENT_PATTERN,
				1);
		return htmlDecode(result);
	}

	/**
	 * Unshortens a short url
	 */
	private String unshortenUrl(String shortURL) {
		if (!shortURL.startsWith(HTTP_PROTOCOL)
				&& !shortURL.startsWith(HTTPS_PROTOCOL))
			return "";

		URLConnection urlConn = connectURL(shortURL);
		urlConn.getHeaderFields();

		String finalResult = urlConn.getURL().toString();

		urlConn = connectURL(finalResult);
		urlConn.getHeaderFields();

		shortURL = urlConn.getURL().toString();

		while (!shortURL.equals(finalResult)) {
			finalResult = unshortenUrl(finalResult);
		}

		return finalResult;
	}

	/**
	 * Takes a valid url and return a URL object representing the url address.
	 */
	private URLConnection connectURL(String strURL) {
		URLConnection conn = null;
		try {
			URL inputURL = new URL(strURL);
			conn = inputURL.openConnection();
		} catch (MalformedURLException e) {
			System.out.println("Please input a valid URL");
		} catch (IOException ioe) {
			System.out.println("Can not connect to the URL");
		}
		return conn;
	}

	/** Removes extra spaces and trim the string */
	public static String extendedTrim(String content) {
		return content.replaceAll("\\s+", " ").replace("\n", " ")
				.replace("\r", " ").trim();
	}




	public String getPriceForTargetWebsite(String content) {


		List<String> matches = Regex.pregMatchAll(content,
				Regex.DIV_PATTERN, 0);

          String price="";

			  List <String> spanMatches=Regex.pregMatchAll(matches.get(0).toString(),
					  Regex.SPAN_PATTERN, 0);
			  price=Regex.pregMatchAll(spanMatches.get(0),Regex.PRICE_PATTERN,0).get(0);













		return  price;
	}
}

