package com.event.event_reservation.chat.service;

import com.event.event_reservation.entity.Event;
import com.event.event_reservation.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatAiService {

    private final ChatClient chatClient;
    private final EventRepository eventRepository;

    private String getDatabaseContext() {
        List<Event> events = eventRepository.findAll();
        if (events.isEmpty()) {
            return "Aucun événement n'est listé dans la base de données pour le moment.";
        }

        return events.stream()
                .map(e -> String.format("• %s\n  Date : %s\n  Lieu : %s, %s\n  Prix : %s MAD\n  Identifiant de lien : /event/%d",
                        e.getTitre().toUpperCase(),
                        e.getDateDebut().format(DateTimeFormatter.ofPattern("dd MMMM yyyy à HH:mm")),
                        e.getLieu(),
                        e.getVille(),
                        e.getPrixUnitaire(),
                        e.getId()))
                .collect(Collectors.joining("\n\n"));
    }

    public String getAiResponse(String userMessage) {
        String systemInstructions = """
            Tu es l'assistant virtuel de 'Occasio Event'. 
            
            RÈGLES CRUCIALES DE COMPORTEMENT :
            1. ANALYSE DE L'INTENTION : 
               - Si l'utilisateur dit 'merci', 'au revoir', 'bonjour' ou fait une simple politesse, réponds poliment SANS lister les événements.
               - N'affiche la liste des événements que si l'utilisateur le demande explicitement ou pose une question sur la programmation.
            
            2. PERTINENCE : 
               - Ne réponds qu'aux questions concernant les événements Occasio Event.
               - Si l'utilisateur te remercie, termine la conversation poliment en demandant s'il a besoin d'autre chose.
            
            3. LIENS : 
               - Uniquement quand tu listes un événement, ajoute OBLIGATOIREMENT : [VOIR_DETAILS](ID_DU_LIEN).
               - Ne mets jamais de lien si tu ne listes pas d'événement.
            
            4. PRINCIPE DE VÉRITÉ : 
               - Ne jamais inventer d'informations. Si tu ne sais pas, dis-le.
            
            5. TON ET FORMAT : 
               - Professionnel, courtois, utilise le vouvoiement.
               - Utilise des listes à puces (•) et des majuscules pour les titres d'événements.
            
            ÉVÉNEMENTS DISPONIBLES DANS LA BASE DE DONNÉES (À utiliser uniquement si pertinent) :
            {context}
            """;

        return chatClient.prompt()
                .system(sp -> sp.text(systemInstructions).param("context", getDatabaseContext()))
                .user(userMessage)
                .call()
                .content();
    }
}