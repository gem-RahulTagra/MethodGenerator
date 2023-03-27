Feature: Test


  Background:
    Given User is on homepage

  @reg
  Scenario: Verify FirstName value
    When User enters "Ayush" as FirstName input
    Then User gets "Ayush" attribute of FirstName element
    Then User verifies the "Ayush" for FirstName element

  @reg
  Scenario: Verify Username field is enabled
    When User enters "Ayush" as FirstName input
    Then User verify the given FirstName element is enabled

  @reg
  Scenario: Verify Username field is empty
    When User enters "Ayush" as FirstName input
    When User clears the FirstName element text
    Then User verifies the " " for FirstName element

  @reg
  Scenario: Verify Username field is visible
    When User clears the FirstName element text
    Then FirstName is visible

  @reg
  Scenario: Verify Password value
    When User enters "Password" as Password input
    Then User verifies the "Password" for Password element

  @reg
  Scenario: Verify Password field is enabled
    When User enters "Password" as Password input
    Then User verify the given Password element is enabled

  @reg
  Scenario: Verify Password field is empty
    When User clears the Password element text
    Then User verifies the " " for Password element

  @reg
  Scenario: Verify Password field is visible
    When User clears the Password element text
    Then Password is visible

#  @GoogleAnswer1
#  Scenario: Navigate to and Verify that Google page has loaded
#
#
#  @GoogleAnswer2
#  Scenario: Switch Languages on Google Home Page
#    Given User navigates to "google"
#    Then change language to Hindi

#  @GoogleAnswer3
#  Scenario: Navigate to Google and Click on the "I'm Feeling Lucky" button
#    Given User navigates to "google"
#    Then Type "serenity bdd" in search bar
#    Then click on "I'm feeling lucky"
##
#  @GoogleAnswer4
#  Scenario Outline: Search for <search> from Google Home Page
#    Given User navigates to "google"
#    Then User verifies navigation to "google"
#    Then Type "<search>" in search bar
#    Then click on "Search"
#
#    Examples:
#      | search   |
#      | serenity |
#      | bdd      |
#
#  @GoogleAnswer5
#  Scenario: Verify that there are no Errors in Navigation
#    Given User navigates to "google"
#    Then User verifies navigation to "google"
#    Then Type "serenity bdd" in search bar
#    Then click on "Search"
#    Then verify there are no navigation errors
#
#  @GoogleAnswer6
#  Scenario: Verify the number of result pages that Google has loaded
#    Given User navigates to "google"
#    Then User verifies navigation to "google"
#    Then Type "serenity bdd" in search bar
#    Then click on "Search"
#    Then Verify the number of result pages that Google has loaded as "8"
#
#  @GoogleAnswer7
#  Scenario: Navigate to and Verify that Google page has loaded
#    Given User navigates to "google"
#    Then User verifies navigation to "google"
#    Then Type "serenity bdd" in search bar
#    Then click on "Search"
#    Then verify that page "https://www.blazemeter.com/blog/rest-assured-api-testing" has loaded in results
#
#  @GoogleAnswer8
#  Scenario: Search for a query and switch to Video results
#    Given User navigates to "google"
#    Then User verifies navigation to "google"
#    Then Type "serenity bdd" in search bar
#    Then click on "Search"
#    Then click on "Videos"
#
#  @GoogleAnswer9
#  Scenario: Search for a query and switch to Image results and screenshot an image
#    Given User navigates to "google"
#    Then User verifies navigation to "google"
#    Then Type "serenity bdd" in search bar
#    Then click on "Search"
#    Then click on "Images"
#    Then take screenshot of image no "3"
#
#  @GoogleAnswer10
#  Scenario: Search for a query and filter search results to Past 24 Hours
#    Given User navigates to "google"
#    Then User verifies navigation to "google"
#    Then Type "serenity bdd" in search bar
#    Then click on "Search"
#    Then Filter search results according to "Past 24 Hours"
#    Then Verify search results are filtered according to "Past 24 Hours"
#
#  @GoogleAnswer11
#  Scenario: Search for a query and filter search results to Past Week
#    Given User navigates to "google"
#    Then User verifies navigation to "google"
#    Then Type "serenity bdd" in search bar
#    Then click on "Search"
#    Then Filter search results according to "Past week"
#    Then Verify search results are filtered according to "Past week"
#    Then Clear search result filter
#
#  @GoogleAnswer12
#  Scenario: Search for a query and Verify all Search Result type tabs are present
#    Given User navigates to "google"
#    Then User verifies navigation to "google"
#    Then Type "serenity bdd" in search bar
#    Then click on "Search"
#    Then Verify presence of all search type tabs
#
#  @GoogleAnswer13
#  Scenario: Search for a query and verify Footer links are Present
#    Given User navigates to "google"
#    Then User verifies navigation to "google"
#    Then Type "serenity bdd" in search bar
#    Then click on "Search"
#    Then Verify presence of footer links