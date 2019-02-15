package angryhexclient.util;

import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

public class HTMLUtil {

	private static Logger Log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private String title;
	private int round;
	private ArrayList<String> pictures;
	private ArrayList<String> textfields;

	public HTMLUtil(String title, int round){
		this.title = title;
		this.round = round;
		pictures = new ArrayList<String>();
		textfields = new ArrayList<String>();
	}

	public void addPicture(String filename){
		pictures.add(filename);
	}

	public void addText(String text){
		textfields.add(text);
	}

	public void renderPage(String filename){
		String result = "";

		result += "<!DOCTYPE html>\n<html>\n<head>\n\t<title>" + title + "</title>\n";
		result += generateCSS();
		result += "</head>\n";
		result += "<body>\n";
		result += "\t<h1> Debugging Marks for " + title + " Round " + round + "</h1>";
		result += "\t<div>\n";
		for(String s: textfields){
			result += "\t\t<p>" + s + "</p>\n";
		}
		result += "\t</div>\n";
		result += "<div class=\"slideshow-container\">\n";
		int c = 1;
		for(String s: pictures){
			String[] arr = s.split(File.separator);

			result += "<div class=\"mySlides fade\">\n";
			result += "<div class=\"numbertext\">"+c+" / "+pictures.size()+"</div>\n";
			result += "\t<img src=\"" + arr[arr.length-1] + "\" style=\"width:100%\">\n";

  			result += "<div class=\"text\">" +arr[arr.length-1]+ "</div>\n";
			result += "</div>\n";
			c++;

		}
		result += "<a class=\"prev\" onclick=\"plusSlides(-1)\">&#10094;</a>\n";
		result += "<a class=\"next\" onclick=\"plusSlides(1)\">&#10095;</a>\n";

		result += "</div>\n";
		result += "<br>\n";

		result += "<div style=\"text-align:center\">\n";

		for(int i = 1; i <= pictures.size(); i++)
 		result += "<span class=\"dot\" onclick=\"currentSlide(" + i +")\"></span>\n"; 
		result += "</div>\n";

		result += generateJavascript();
		result += "</body>\n</html>";
		
		File file = new File(filename + File.separator + String.format("debug_%d.html",round));
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(result);
			bw.close();
			Log.fine("saved HTML file for debugging");
		} catch(IOException I){
			Log.warning("could not save HTML file: " + I.getMessage());
		}
	}

	private String generateCSS(){
		return "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n<style>\n* {box-sizing: border-box}\nbody {font-family: Verdana, sans-serif; margin:0}\n.mySlides\n{display: none}\nimg {vertical-align: middle;}\n\n/* Slideshow container */\n.slideshow-container {\n  max-width: 1000px;\n  position: relative;\n  margin: auto;\n}\n\n/* Next & previous buttons */\n.prev, .next {\n  cursor: pointer;\n  position: absolute;\n  top: 50%;\n  width: auto;\n  padding: 16px;\n  margin-top: -22px;\n  color: white;\n  font-weight: bold;\n  font-size: 18px;\n  transition: 0.6s ease;\n  border-radius: 0 3px 3px 0;\n}\n\n/* Position the \"next button\" to the right */\n.next {\n: 0;\n  border-radius: 3px 0 0 3px;\n}\n\n/* On hover, add a black background color with a little bit see-through */\n.prev:hover, .next:hover {\n  background-color: rgba(0,0,0,0.8);\n}\n\n/* Caption text */\n.text {\n  color: #f2f2f2;\n  font-size: 15px;\n  padding: 8px 12px;\n  position: absolute;  bottom: 8px;\n  width: 100%;\n  text-align: center;\n}\n\n/* Number text (1/3 etc) */\n.numbertext {\n  color: #f2f2f2;\n  font-size: 12px;\n  padding: 8px 12px;\n  position: absolute;\n  top: 0;\n}\n\n/* The dots/bullets/indicators */\n.dot {\n  cursor: pointer;\n  height: 15px;\n  width: 15px;\n  margin: 0 2px;\n  background-color: #bbb;\n  border-radius: 50%;\n  display: inline-block;\n  transition: background-color 0.6s ease;\n}\n\n.active, .dot:hover {\n  background-color: #717171;\n}\n\n/* Fading animation */\n.fade {\n  -webkit-animation-name: fade;\n  -webkit-animation-duration: 1.5s;\n  animation-name: fade;\n  animation-duration: 1.5s;\n}\n\n@-webkit-keyframes fade {\n  from {opacity: .4}\n  to {opacity: 1}\n}\n\n@keyframes fade {\n  from {opacity: .4}\n  to {opacity: 1}\n}\n\n/* On smaller screens, decrease text size */\n@media only screen and (max-width: 300px) {\n  .prev, .next,.text {font-size: 11px}\n}\n</style>\n";
	}

	private String generateJavascript(){
		return "<script>\nvar slideIndex = 1;\nshowSlides(slideIndex);\n\nfunction plusSlides(n) {\n  showSlides(slideIndex += n);\n}\n\nfunction currentSlide(n) {\n showSlides(slideIndex = n);\n}\n\nfunction showSlides(n) {\n  var i;\n  var slides = document.getElementsByClassName(\"mySlides\");\n  var dots = document.getElementsByClassName(\"dot\");\n  if (n > slides.length) {slideIndex = 1}\n\n  if (n < 1) {slideIndex = slides.length}\n  for (i = 0; i < slides.length; i++) {\n      slides[i].style.display = \"none\";\n  }\n  for (i = 0; i < dots.length; i++) {\n      dots[i].className = dots[i].className.replace(\" active\", \"\");\n  }\n  slides[slideIndex-1].style.display = \"block\";\n  dots[slideIndex-1].className += \" active\";\n}\n</script>\n";
	}
}
