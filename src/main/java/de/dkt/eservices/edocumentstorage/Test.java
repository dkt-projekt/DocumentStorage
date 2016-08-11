package de.dkt.eservices.edocumentstorage;

import java.util.regex.Pattern;

public class Test {

	public static void main(String[] args) {
		
		String regex = "[a-zA-Z0-9\\-_]*";
		
		System.out.println(Pattern.matches(regex, "abc"));
		System.out.println(Pattern.matches(regex, "abc*"));
		System.out.println(Pattern.matches(regex, "ab.c"));
		System.out.println(Pattern.matches(regex, "a2bc"));
		System.out.println(Pattern.matches(regex, "ab-_c"));
	}

}
