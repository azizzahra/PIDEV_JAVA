package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Badge;
import model.user;
import services.BadgeService;
import services.UserService;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class BadgeStatisticsController implements Initializable {

    @FXML private PieChart badgeTypePieChart;
    @FXML private BarChart<String, Number> userBadgesBarChart;
    @FXML private Label totalBadgesLabel;
    @FXML private Label mostCommonTypeLabel;
    @FXML private Label recentBadgesLabel;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private TableView<BadgeStatItem> badgeStatTable;
    @FXML private TableColumn<BadgeStatItem, String> typeColumn;
    @FXML private TableColumn<BadgeStatItem, Integer> countColumn;

    private final BadgeService badgeService = new BadgeService();
    private final UserService userService = new UserService(); // Assurez-vous d'avoir un service utilisateur
    private List<Badge> allBadges;

    // Classe interne pour les statistiques de badges
    public static class BadgeStatItem {
        private final String type;
        private final int count;

        public BadgeStatItem(String type, int count) {
            this.type = type;
            this.count = count;
        }

        public String getType() {
            return type;
        }

        public int getCount() {
            return count;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser les colonnes du tableau
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        countColumn.setCellValueFactory(new PropertyValueFactory<>("count"));

        // Initialiser le ComboBox avec les options de filtrage
        filterComboBox.getItems().addAll("Tous", "Aujourd'hui", "Cette semaine", "Ce mois-ci");
        filterComboBox.setValue("Tous");
        filterComboBox.setOnAction(e -> updateStatistics());

        // Charger les données initiales
        loadData();
        updateStatistics();
    }

    private void loadData() {
        // Charger tous les badges de la base de données
        allBadges = badgeService.getAllBadges();
    }

    @FXML
    private void refreshData() {
        loadData();
        updateStatistics();
    }

    private void updateStatistics() {
        // Filtrer les badges selon l'option sélectionnée
        List<Badge> filteredBadges = filterBadges();

        // Mettre à jour les statistiques globales
        updateGlobalStats(filteredBadges);

        // Mettre à jour le graphique en camembert des types de badges
        updateBadgeTypePieChart(filteredBadges);

        // Mettre à jour le graphique à barres des badges par utilisateur
        updateUserBadgesBarChart(filteredBadges);

        // Mettre à jour le tableau des statistiques
        updateBadgeStatTable(filteredBadges);
    }

    private List<Badge> filterBadges() {
        String filter = filterComboBox.getValue();
        if (filter == null || filter.equals("Tous")) {
            return allBadges;
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate;

        switch (filter) {
            case "Aujourd'hui":
                startDate = today;
                break;
            case "Cette semaine":
                startDate = today.minusDays(today.getDayOfWeek().getValue() - 1);
                break;
            case "Ce mois-ci":
                startDate = today.withDayOfMonth(1);
                break;
            default:
                return allBadges;
        }

        // Utiliser dateAttribution au lieu de dateCreation
        return allBadges.stream()
                .filter(badge -> {
                    LocalDate badgeDate = badge.getDateAttribution();
                    return badgeDate != null && !badgeDate.isBefore(startDate);
                })
                .collect(Collectors.toList());
    }

    private void updateGlobalStats(List<Badge> badges) {
        // Nombre total de badges
        totalBadgesLabel.setText("Nombre total de badges : " + badges.size());

        // Type de badge le plus courant
        if (!badges.isEmpty()) {
            Map<String, Long> typeCounts = badges.stream()
                    .collect(Collectors.groupingBy(Badge::getType, Collectors.counting()));

            String mostCommonType = typeCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");

            mostCommonTypeLabel.setText("Type de badge le plus courant : " + mostCommonType);
        } else {
            mostCommonTypeLabel.setText("Type de badge le plus courant : N/A");
        }

        // Badges récents
        long recentBadges = badges.stream()
                .filter(badge -> badge.getDateAttribution() != null &&
                        badge.getDateAttribution().isAfter(LocalDate.now().minusDays(7)))
                .count();

        recentBadgesLabel.setText("Badges attribués cette semaine : " + recentBadges);
    }

    private void updateBadgeTypePieChart(List<Badge> badges) {
        // Calculer le nombre de badges par type
        Map<String, Long> typeCounts = badges.stream()
                .collect(Collectors.groupingBy(Badge::getType, Collectors.counting()));

        // Créer les données pour le graphique en camembert
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Long> entry : typeCounts.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        // Mettre à jour le graphique
        badgeTypePieChart.setData(pieChartData);
        badgeTypePieChart.setTitle("Répartition des badges par type");
    }

    private void updateUserBadgesBarChart(List<Badge> badges) {
        // Calculer le nombre de badges par utilisateur
        Map<Integer, Long> userBadgeCounts = badges.stream()
                .collect(Collectors.groupingBy(Badge::getUserId, Collectors.counting()));

        // Créer une série pour le graphique à barres
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre de badges");

        // Limiter aux 10 premiers utilisateurs pour la lisibilité
        userBadgeCounts.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    String userName = getUserName(entry.getKey());
                    series.getData().add(new XYChart.Data<>(userName, entry.getValue()));
                });

        // Mettre à jour le graphique
        userBadgesBarChart.getData().clear();
        userBadgesBarChart.getData().add(series);
        userBadgesBarChart.setTitle("Badges par utilisateur (Top 10)");
    }

    private void updateBadgeStatTable(List<Badge> badges) {
        // Calculer le nombre de badges par type
        Map<String, Long> typeCounts = badges.stream()
                .collect(Collectors.groupingBy(Badge::getType, Collectors.counting()));

        // Créer les données pour le tableau
        ObservableList<BadgeStatItem> tableData = FXCollections.observableArrayList();
        for (Map.Entry<String, Long> entry : typeCounts.entrySet()) {
            tableData.add(new BadgeStatItem(entry.getKey(), entry.getValue().intValue()));
        }

        // Mettre à jour le tableau
        badgeStatTable.setItems(tableData);
    }

    private String getUserName(int userId) {
        // Obtenir le nom de l'utilisateur à partir de son ID
        // Adaptez selon votre modèle de données
        try {
            user user = userService.getUserById(userId);
            return user != null ? user.getPrenom() : "Utilisateur " + userId;
        } catch (Exception e) {
            return "Utilisateur " + userId;
        }
    }

    // Méthode pour l'export des statistiques
    @FXML
    private void exportStatistics() {
        // À implémenter selon vos besoins spécifiques
        System.out.println("Fonctionnalité d'exportation à implémenter");
    }
}