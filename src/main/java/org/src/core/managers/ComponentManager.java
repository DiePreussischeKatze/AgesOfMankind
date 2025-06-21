package org.src.core.managers;

import org.src.core.helper.Component;

import java.util.ArrayList;
import java.util.List;

public final class ComponentManager {
	private final ArrayList<Component> components;

	public ComponentManager() {
		components = new ArrayList<>();
	}

	public void draw() {
		components.forEach(Component::draw);
	}

	public void update(final double deltaTime) {
		components.forEach(component -> component.update(deltaTime));
	}

	public void addComponent(final Component... components) {
		this.components.addAll(List.of(components));
	}

	public void addComponentToStart(final Component component) {
		this.components.addFirst(component);
	}

	public void removeComponent(final Component component) {
		this.components.remove(component);
	}

	public void dispose() {
		this.components.forEach(Component::dispose);
	}

}
