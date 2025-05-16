package services;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ProfanityFilter {
    private final Trie trie;

    // Liste par défaut des mots inappropriés en français et en anglais
    private static final Set<String> DEFAULT_BAD_WORDS = new HashSet<>(Arrays.asList(
            // Insultes et grossièretés en français et anglais
            "connard", "connasse", "con", "salope", "pute", "putain", "merde", "enculé", "enculer",
            "bite", "couilles", "pd", "pédé", "negro", "négro", "nègre", "bougnoule", "bicot",
            "fuck", "bitch", "shit", "asshole", "cunt", "whore", "cock", "dick", "pussy", "nigger",
            "faggot", "slut", "motherfucker", "bastard", "twat",

            // Mots offensants et discriminatoires
            "pute", "salope", "tapette", "tarlouze", "tantouze", "travelo", "tafiole",
            "retard", "spaz", "cripple", "midget", "retarded",

            // Violence
            "suicide", "kill", "murder", "rape", "terrorist", "bomb", "shoot",

            // Autres termes inappropriés
            "nazi", "hitler", "holocaust", "genocide", "pedophile", "pédophile"

            // Note: Cette liste n'est pas exhaustive et peut être complétée selon vos besoins
    ));


    /**
     * Constructeur par défaut qui utilise la liste prédéfinie de mots inappropriés
     */
    public ProfanityFilter() {
        this(DEFAULT_BAD_WORDS);
    }
    private static Set<String> loadBadWordsFromFile(String filePath) {
        Set<String> badWords = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                badWords.add(line.trim().toLowerCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return badWords;
    }

    /**
     * Constructeur avec une liste personnalisée de mots inappropriés
     * @param badWords Collection de mots à filtrer
     */
    public ProfanityFilter(Collection<String> badWords) {
        Trie.TrieBuilder builder = Trie.builder();
        for (String word : badWords) {
            builder.addKeyword(word.toLowerCase());
        }
        this.trie = builder.build();
    }

    /**
     * Vérifie si le texte contient des mots inappropriés
     * @param text Texte à vérifier
     * @return true si des mots inappropriés sont trouvés, false sinon
     */
    public boolean containsProfanity(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        Collection<Emit> emits = trie.parseText(text.toLowerCase());
        return !emits.isEmpty();
    }

    /**
     * Censure les mots inappropriés dans le texte en les remplaçant par des astérisques
     * @param text Texte à censurer
     * @return Texte censuré
     */
    public String censorText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;
        Collection<Emit> emits = trie.parseText(text.toLowerCase());

        for (Emit emit : emits) {
            int start = emit.getStart();
            int end = emit.getEnd() + 1;

            // S'assurer que les indices sont valides
            if (start >= 0 && end <= text.length() && start < end) {
                String match = text.substring(start, end);
                String censored = "*".repeat(match.length());
                // Remplacer le mot inapproprié par des astérisques
                result = result.replace(match, censored);
            }
        }

        return result;
    }

    /**
     * Retourne la liste des mots inappropriés trouvés dans le texte
     * @param text Texte à analyser
     * @return Collection des mots inappropriés trouvés
     */
    public Collection<String> findProfanities(String text) {
        if (text == null || text.isEmpty()) {
            return new HashSet<>();
        }

        Set<String> found = new HashSet<>();
        Collection<Emit> emits = trie.parseText(text.toLowerCase());

        for (Emit emit : emits) {
            found.add(text.substring(emit.getStart(), emit.getEnd() + 1));
        }

        return found;
    }
}