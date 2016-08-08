package de.turtle.imp.modules;

import java.util.HashMap;
import de.turtle.imp.modules.*;

import de.turtle.imp.models.User;

public class AuthUsers {
	public static int authUser(User user, String password) {
		HashMap<String, String> auth = new HashMap<>();
		auth.put("admin", "456b7016a916a4b178dd72b947c152b7");
		auth.put("user", "ee11cbb19052e40b07aac0ca060c23ee");

		if (auth.get(user.getUserName()).equals(password)) {
			if (user.getUserName().equals("admin")) {
				return 100;
			} else if (user.getUserName().equals("user")) {
				return 1;
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}
}
