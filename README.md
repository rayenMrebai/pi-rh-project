# Integra RH – Application Desktop Intelligente de Gestion RH 

<p align="center">
  <img src="https://github.com/rayenMrebai/pi-rh-project/blob/e6aef2d7852dc8ab638d047d6dcd1b4517e3c228/java.png?raw=true" alt="Java Logo" width="600">
</p>

## Sommaire

* [Description](#description)
* [Fonctionnalités principales](#fonctionnalités-principales)
  * [👥 Gestion des utilisateurs](#-gestion-des-utilisateurs)
  * [📢 Gestion du recrutement](#-gestion-du-recrutement)
  * [🎓 Gestion des formations](#-gestion-des-formations)
  * [💰 Gestion des salaires](#-gestion-des-salaires)
  * [📁 Gestion de projets et affectations](#-gestion-de-projets-et-affectations)
  * [📊 Dashboard & Statistiques](#-dashboard--statistiques)
* [Fonctionnalités IA](#fonctionnalités-ia)
* [Technologies utilisées](#technologies-utilisées)
* [Architecture](#architecture)
* [Fonctionnalités avancées](#fonctionnalités-avancées)
* [Installation](#installation)
* [Améliorations futures](#améliorations-futures)
* [Équipe](#équipe)
* [Encadrants](#encadrants)

---

## Description

Integra RH est une application desktop intelligente de gestion des ressources humaines développée avec **Java** et **JavaFX** dans le cadre d’un projet intégré d’ingénierie à **ESPRIT**.

L’objectif du projet est d’automatiser plusieurs processus RH grâce à des outils modernes, des dashboards interactifs et des services d’intelligence artificielle.

Cette version Desktop couvre principalement :

* Gestion des utilisateurs
* Gestion du recrutement
* Gestion des formations
* Gestion des salaires
* **Gestion de projets et des affectations d’employés**

Le système offre une expérience RH avancée avec une interface moderne, responsive et multi-plateforme.

---

## Fonctionnalités principales

### 👥 Gestion des utilisateurs

* Authentification sécurisée avec BCrypt
* Gestion des rôles (Admin / Manager / Employé)
* CRUD complet des utilisateurs
* Récupération du mot de passe par email
* Notifications internes
* Recherche multicritères
* Export PDF
* Interface moderne JavaFX
* Mode clair/sombre

### 📢 Gestion du recrutement

* Gestion des offres d’emploi
* Gestion des candidats
* Détection de doublons
* Gestion des statuts RH
* Analyse intelligente des CV avec IA
* Calcul ATS Score
* Génération automatique de lettres de refus
* Planification des entretiens
* Envoi automatique d’emails
* Dashboard RH interactif
* Export PDF
* Notifications Telegram
* QR Code Android

### 🎓 Gestion des formations

* Catalogue de formations via API Coursera
* Recherche de compétences via ESCO
* Génération automatique de quiz avec IA
* Correction automatique des quiz
* Gestion des résultats
* Sauvegarde des scores
* Système anti-doublon
* Fallback hors ligne

### 💰 Gestion des salaires

* Gestion complète des salaires
* Calcul automatique des bonus
* Gestion des états de paiement
* Génération de fiches de paie PDF
* Export Excel avec statistiques
* Envoi automatique d’emails
* Dashboard statistique
* Graphiques interactifs QuickChart
* Prédiction salariale par régression linéaire

### 📁 Gestion de projets et affectations

* CRUD complet des projets et des affectations d’employés
* Recherche et filtrage avancés (statut, dates, projet, rôle)
* Validation métier côté serveur
* Export PDF (iText) et Excel (Apache POI) avec statistiques et graphiques
* Assistant IA intégré (résumé, amélioration de description, traduction)
* Recommandation d’employés par similarité sémantique (IA)
* Dictée vocale pour la description des projets
* Taux de change en direct (USD/TND, EUR/TND)

### 📊 Dashboard & Statistiques

* KPIs RH interactifs
* Graphiques dynamiques
* Suivi des utilisateurs actifs/inactifs

---

## Fonctionnalités IA

Le système intègre plusieurs fonctionnalités d’intelligence artificielle :

* 🤖 Analyse automatique des CV
* 🧠 Extraction intelligente des compétences
* 💡 Recommandations RH
* 📈 Calcul ATS Score
* 📝 Génération automatique de quiz
* ✉️ Génération de lettres RH
* 📊 Prédiction salariale basée sur la régression linéaire
* 🧠 Assistant de projet (résumé, amélioration, traduction)
* 🧑‍💼 Recommandation d’employés pour un projet
* 🎤 Dictée vocale pour les descriptions de projet

---

## Technologies utilisées

### Application Desktop

| Technologie | Version |
|-------------|---------|
| Java | 17 |
| JavaFX | 21 |
| JDBC | - |
| Maven | - |

### Base de données

* MySQL

### APIs & Services

* Hugging Face API
* Groq API
* Coursera RapidAPI
* ESCO Europe API
* Gmail API
* QuickChart API
* Ollama (LLM local)

### Bibliothèques

* JavaFX Controls
* JavaFX FXML
* OpenPDF
* Apache POI
* JavaMail
* MySQL Connector

### Tests

* JUnit 5

### Fonctionnalités UI

* Interface responsive JavaFX
* Mode clair/sombre
* Dashboard interactif
* Application plein écran

---

## Architecture

Le projet suit une architecture en **couches** :

* **Controllers** - Gestion des événements UI
* **Services** - Logique métier
* **DAO / JDBC** - Accès aux données
* **Entités** - Modèles de données
* **Interfaces JavaFX** - Vues FXML

Le système communique avec :

* APIs IA
* Services REST
* Base de données SQL
* Notifications Telegram
* Services email

---

## Fonctionnalités avancées

* Automatisation intelligente du recrutement
* Analyse RH assistée par IA
* Notifications temps réel
* Dashboards interactifs
* Export PDF & Excel
* Accès mobile via QR Code
* Système multi-plateforme
* Fallback hors ligne

---

## Installation

### Prérequis

- Java 17 ou supérieur
- Maven
- MySQL

### Cloner le projet

```bash
git clone https://github.com/rayenMrebai/pi-rh-project.git
cd pi-rh-project
