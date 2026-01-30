package com.event.event_reservation.chat.view;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatMessageElement extends HorizontalLayout {

    public ChatMessageElement(String message, boolean isUser) {
        setSpacing(true);
        setWidthFull();
        setAlignItems(Alignment.START);

        Avatar avatar = new Avatar(isUser ? "Moi" : "AI");
        avatar.getStyle().set("background-color", isUser ? "#bdc3c7" : "#253366");

        VerticalLayout bubbleWrapper = new VerticalLayout();
        bubbleWrapper.setPadding(false);
        bubbleWrapper.setSpacing(false);
        bubbleWrapper.setAlignItems(isUser ? Alignment.END : Alignment.START);

        Div bubble = new Div();
        var s = bubble.getStyle();
        s.set("padding", "12px 18px");
        s.set("border-radius", "15px");
        s.set("font-size", "0.95em");
        s.set("line-height", "1.5");
        s.set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");
        s.set("white-space", "pre-wrap");

        if (isUser) {
            s.set("background-color", "#253366");
            s.set("color", "white");
            bubble.setText(message);
        } else {
            s.set("background-color", "white");
            s.set("color", "#1a1a1a");
            s.set("border", "1px solid #e0e6ed");

            // --- LOGIQUE DE DÉTECTION DES LIENS ---
            parseMessageWithLinks(bubble, message);
        }

        bubbleWrapper.add(bubble);

        if (isUser) {
            setJustifyContentMode(JustifyContentMode.END);
            add(bubbleWrapper, avatar);
        } else {
            setJustifyContentMode(JustifyContentMode.START);
            add(avatar, bubbleWrapper);
        }
    }
    private void parseMessageWithLinks(Div container, String text) {
        // On cherche le pattern [VOIR_DETAILS](/event/123) ou simplement /event/123
        Pattern pattern = Pattern.compile("\\[VOIR_DETAILS\\]\\(/event/(\\d+)\\)|/event/(\\d+)");
        Matcher matcher = pattern.matcher(text);

        int lastEnd = 0;
        while (matcher.find()) {
            // Ajouter le texte avant le lien
            container.add(new Span(text.substring(lastEnd, matcher.start())));

            // Récupérer l'ID (soit du premier groupe, soit du deuxième selon le pattern trouvé)
            String id = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);

            // Créer le lien cliquable style bouton
            Anchor link = new Anchor("event/" + id, " ➔ Voir l'événement ");
            link.getStyle()
                    .set("color", "#253366")
                    .set("font-weight", "bold")
                    .set("text-decoration", "underline")
                    .set("background", "#f1f3f9")
                    .set("padding", "2px 8px")
                    .set("border-radius", "5px")
                    .set("margin", "0 5px");

            container.add(link);
            lastEnd = matcher.end();
        }
        // Ajouter le reste du texte après le dernier lien
        container.add(new Span(text.substring(lastEnd)));
    }
}