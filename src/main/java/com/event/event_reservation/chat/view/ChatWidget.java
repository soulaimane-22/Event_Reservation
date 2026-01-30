package com.event.event_reservation.chat.view;

import com.event.event_reservation.chat.service.ChatAiService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class ChatWidget extends VerticalLayout {
    private final VerticalLayout messageList = new VerticalLayout();
    private final TextField inputField = new TextField();
    private final ChatAiService chatAiService;
    private final String ICON_PATH = "images/events/icons/";

    public ChatWidget(ChatAiService chatAiService) {
        this.chatAiService = chatAiService;

        setWidth("500px");
        setHeight("560px");
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "white")
                .set("border-radius", "20px")
                .set("box-shadow", "0 15px 50px rgba(0,0,0,0.25)")
                .set("overflow", "hidden");

        HorizontalLayout header = new HorizontalLayout(new H4("OccasioBot Assistant"));
        header.setWidthFull();
        header.getStyle().set("background-color", "#253366").set("padding", "15px 20px");
        header.getChildren().forEach(c -> c.getStyle().set("color", "white").set("margin", "0"));

        messageList.setPadding(true);
        messageList.setSpacing(true);
        Scroller scroller = new Scroller(messageList);
        scroller.setSizeFull();

        HorizontalLayout inputArea = new HorizontalLayout();
        inputArea.setWidthFull();
        inputArea.setPadding(true);
        inputArea.setAlignItems(Alignment.CENTER);
        inputArea.getStyle().set("border-top", "1px solid #f0f2f5");

        inputField.setPlaceholder("Posez votre question...");
        inputField.setWidthFull();
        inputField.getStyle().set("border-radius", "10px");

        Image sendIcon = new Image(ICON_PATH + "send.svg", "Envoyer");
        sendIcon.setWidth("28px");
        sendIcon.setHeight("28px");

        Button sendBtn = new Button(sendIcon);
        sendBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY); // Supprime le style par défaut

        var s = sendBtn.getStyle();
        s.set("background", "none")
                .set("border", "none")
                .set("padding", "0")
                .set("min-width", "40px")
                .set("cursor", "pointer")
                .set("transition", "transform 0.2s ease, opacity 0.2s ease");

        // Effet Hover via JavaScript injecté en Java
        sendBtn.getElement().executeJs(
                "this.onmouseover = () => { this.style.transform = 'scale(1.2)'; this.style.opacity = '0.8'; };" +
                        "this.onmouseout = () => { this.style.transform = 'scale(1)'; this.style.opacity = '1'; };"
        );

        sendBtn.addClickListener(e -> sendMessage());
        inputField.addKeyPressListener(Key.ENTER, e -> sendMessage());

        inputArea.add(inputField, sendBtn);
        add(header, scroller, inputArea);

        // Message de bienvenue
        messageList.add(new ChatMessageElement("Bonjour ! Je suis l'assistant Occasio. Comment puis-je vous aider ?", false));
    }

    private void sendMessage() {
        String text = inputField.getValue();
        if (text != null && !text.trim().isEmpty()) {
            messageList.add(new ChatMessageElement(text, true));
            inputField.clear();
            UI_Update(text);
        }
    }

    private void UI_Update(String text) {
        try {
            String response = chatAiService.getAiResponse(text);
            messageList.add(new ChatMessageElement(response, false));
            // Scroll vers le bas automatique
            getElement().executeJs("const s = this.querySelector('vaadin-scroller'); if(s) s.scrollTop = s.scrollHeight;");
        } catch (Exception ex) {
            messageList.add(new ChatMessageElement("Désolé, je rencontre une difficulté technique.", false));
        }
    }
}