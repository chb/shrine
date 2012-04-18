package net.shrine.webclient.client.widgets;

import java.util.List;

import net.shrine.webclient.client.util.Util;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Mar 28, 2012
 */
public final class Spinner extends Composite {

	private static SpinnerUiBinder uiBinder = GWT.create(SpinnerUiBinder.class);

	interface SpinnerUiBinder extends UiBinder<Widget, Spinner> { }

	@UiField
	TextBox textBox;

	@UiField
	Image upArrow;

	@UiField
	Image downArrow;

	private int min = 0;
	private int max = Integer.MAX_VALUE;
	private int step = 1;
	private int value = 0;

	public interface SpinnerHandler {
		void onValueChange(final int newValue);
	}

	private final List<SpinnerHandler> handlers = Util.makeArrayList();

	private ArrowHandler upArrowHandler;

	private ArrowHandler downArrowHandler;

	public Spinner() {
		super();
		
		initWidget(uiBinder.createAndBindUi(this));
		
		initTextBox();

		handlers.add(new SpinnerHandler() {
			@Override
			public void onValueChange(final int newValue) {
				textBox.setText(String.valueOf(newValue));
			}
		});

		this.upArrowHandler = new ArrowHandler(upArrow, Urls.UpArrow) {
			@Override
			void changeValue() {
				increment();
			}
		};

		this.downArrowHandler = new ArrowHandler(downArrow, Urls.DownArrow) {
			@Override
			void changeValue() {
				decrement();
			}
		};

		initUpArrow();

		initDownArrow();
	}

	public void setWidth(final String width) {
		this.textBox.setWidth(width);
	}

	private final boolean isAllowedChar(final char ch) {
		Log.debug("Char: " + ch + " (" + ((int)ch) + ")");
		
		final boolean result = !Character.isLetter(ch);//Character.isDigit(ch) || ch == KeyCodes.KEY_ALT || ch == KeyCodes.KEY_CTRL || ch == KeyCodes.KEY_SHIFT || ch == KeyCodes.KEY_BACKSPACE || ch == KeyCodes.KEY_DELETE || ch == KeyCodes.KEY_UP || ch == KeyCodes.KEY_DOWN || ch == KeyCodes.KEY_LEFT || ch == KeyCodes.KEY_RIGHT;
		
		Log.debug("Allowed? " + result);
		
		return result;
	}

	private void initTextBox() {
		this.textBox.setText(String.valueOf(value));

		// Only allow digits (from GWT javadoc:
		// https://google-web-toolkit.googlecode.com/svn/javadoc/2.4/com/google/gwt/user/client/ui/TextBox.html)
		this.textBox.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(final KeyPressEvent event) {
				if (!isAllowedChar(event.getCharCode())) {
					((TextBox) event.getSource()).cancelKey();
				}
			}
		});

		// Make sure we run our handlers when a new value is typed in
		// Using a KeyUpHandler is necessary because value chage handlers are
		// only invoked
		// after something new is typed AND focus leaves the widget :(
		this.textBox.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				update(getTextBoxValue());
			}

			private Integer getTextBoxValue() {
				final String text = textBox.getText();

				if (text == null || text.trim().length() == 0) {
					return null;
				} else {
					return Integer.valueOf(text);
				}
			}
		});
	}

	private void increment() {
		update(clamp(value + step));
	}

	private void decrement() {
		update(clamp(value - step));
	}

	private void update(final Integer newValue) {
		update(newValue, true);
	}
	
	private void update(final Integer newValue, final boolean fireEvents) {
		if (newValue != null && value != newValue) {
			value = newValue;

			if(fireEvents) {
				runHandlers();
			}
		}
	}

	private void runHandlers() {
		final int current = this.value;

		for (final SpinnerHandler handler : handlers) {
			handler.onValueChange(current);
		}
	}

	private int clamp(final int i) {
		if (i < min) {
			return min;
		} else if (i >= max) {
			return max;
		} else {
			return i;
		}
	}

	private void initUpArrow() {
		upArrow.addMouseDownHandler(upArrowHandler);

		upArrow.addMouseUpHandler(upArrowHandler);

		upArrow.addMouseOverHandler(upArrowHandler);

		upArrow.addMouseOutHandler(upArrowHandler);
	}

	private void initDownArrow() {
		downArrow.addMouseDownHandler(downArrowHandler);

		downArrow.addMouseUpHandler(downArrowHandler);

		downArrow.addMouseOverHandler(downArrowHandler);

		downArrow.addMouseOutHandler(downArrowHandler);
	}

	public void addSpinnerHandler(final SpinnerHandler handler) {
		Util.requireNotNull(handler);

		handlers.add(handler);
	}

	public void removeSpinnerHandler(final SpinnerHandler handler) {
		Util.requireNotNull(handler);

		handlers.remove(handler);
	}

	public int getMin() {
		return min;
	}

	public void setMin(final int min) {
		this.min = min;

		update(clamp(value));
	}

	public int getMax() {
		return max;
	}

	public void setMax(final int max) {
		this.max = max;

		update(clamp(value));
	}

	public int getStep() {
		return step;
	}

	public void setStep(final int step) {
		this.step = step;
	}

	public int getValue() {
		return value;
	}

	public void setValue(final int newValue, final boolean fireEvents) {
		update(clamp(newValue), fireEvents);
	}
	
	public void setValue(final int newValue) {
		setValue(newValue, true);
	}

	private abstract class ArrowHandler implements MouseDownHandler, MouseUpHandler, MouseOverHandler, MouseOutHandler {
		private final Image arrow;

		private final ImageUrls urls;

		abstract void changeValue();

		ArrowHandler(final Image arrow, final ImageUrls urls) {
			super();

			Util.requireNotNull(arrow);
			Util.requireNotNull(urls);

			this.arrow = arrow;
			this.urls = urls;
		}

		@Override
		public void onMouseDown(final MouseDownEvent event) {
			changeValue();

			arrow.setUrl(urls.pressed);
		}

		@Override
		public void onMouseUp(final MouseUpEvent event) {
			arrow.setUrl(urls.normal);
		}

		@Override
		public void onMouseOver(final MouseOverEvent event) {
			arrow.setUrl(urls.hover);
		}

		@Override
		public void onMouseOut(final MouseOutEvent event) {
			arrow.setUrl(urls.normal);
		}
	}

	// NB: do this dumbly with Image instances, just to get it working
	private static final class ImageUrls {
		public final String normal;
		public final String pressed;
		public final String hover;
		public final String disabled;

		ImageUrls(final String normal, final String pressed, final String hover, final String disabled) {
			super();

			this.normal = normal;
			this.pressed = pressed;
			this.hover = hover;
			this.disabled = disabled;
		}
	}

	private static final class Urls {
		public static final ImageUrls UpArrow = new ImageUrls("/images/arrowUp.png", "/images/arrowUpPressed.png", "/images/arrowUpHover.png", "/images/arrowUpDisabled.png");

		public static final ImageUrls DownArrow = new ImageUrls("/images/arrowDown.png", "/images/arrowDownPressed.png", "/images/arrowDownHover.png", "/images/arrowDownDisabled.png");
	}
}
