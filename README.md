<div id="top"></div>

[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![LinkedIn][linkedin-shield]][linkedin-url]

<!-- PROJECT LOGO -->
<br />
<div align="center">
 
 ![Logo][skeletonLogo]
 
  <br />
  <h3 align="center">Cold Chain Monitoring App</h3>

  <p align="center">
    The Cold Chain Monitoring App is a capture tool for temperature readings in cold chain supply chains.
    <br />
    <br />
    <a href="https://player.vimeo.com/video/649687929?h=ad4e689fd3"><strong>View Demo »</strong></a>
    <br />
  </p>
</div>




### About The Project
---
The Cold Chain Monitoring App is a capture tool for temperature readings, designed to capture temperatures from a Bluetooth Low Energy (BLE) device and uploaded it to the DHIS2 tracker capter tool.
 
# Introduction
The DHIS2 *Skeleton App* served as a starting point for making the integration with tracker capture using **DHIS2 Android Sdk**. In addition is the UART-Protocol is used for communicating with Bluetooth Low Eenergy (BLE) devices to capture temperature data. 


## Architecture
Images below represent the system arcitechture - how the data flows from the android application to the DHIS2 servers.
![dataFlow](https://user-images.githubusercontent.com/32879828/168015868-2fdeab7f-8439-4c34-93c9-867013234aea.jpg)
System Architecture


Images below represent the application arcitechture - which DHIS2 modules are invloved in the integration and witch modules developed for the BLE communication.
![AppArch4](https://user-images.githubusercontent.com/32879828/168015524-d7f2c725-6d76-4848-bec3-0ab1ae2babb5.jpg)
Application Architecture



## How the app looks


![MainView](https://user-images.githubusercontent.com/32879828/168016493-58468ca6-4a1a-41d8-bae9-35bfd5fdd06a.jpg)

![afterCon](https://user-images.githubusercontent.com/32879828/168016540-36265e2c-47ae-4774-8411-5a9d4bfc7234.jpg)


This app allows to: 

* Login/Logout of DHIS2
* Download metadata from DHIS2
* Download data from DHI2
* Upload data to DHI2
* Wipe data from android device
* Export data from local database to .CSV-file
* Set notification localy on the android device
* Capture temperature readings from BLE device like: current temp, min 24h temp, max 24h temp and average 24h temp.



# Use cases
This application can provide better CCM in all the stages in a supply chain; at health facilities, during transport of CCE and vaccination at rural locations. 
 

# Demo of Appliation
Demo: [https://www.youtube.com/watch?v=GtM8DGe6iUE](https://www.youtube.com/watch?v=GtM8DGe6iUE)

# Application Pilot
If you want to read more about the use-case of this application, here is link describing the application being piloted in a real-life context.
Piloting artical: [https://community.dhis2.org/t/developing-and-piloting-a-dhis2-android-app-for-cold-chain-monitoring/47087](https://community.dhis2.org/t/developing-and-piloting-a-dhis2-android-app-for-cold-chain-monitoring/47087)
 
<!-- CONTACT -->
## Contact
Filip Christoffer Larsen -  Filip.larsen@live.no
<br />
Project Link: [https://github.com/Filipcl/CCM-App](https://github.com/Filipcl/CCM-App)
<br />
DHIS2 Skeleton App: [https://github.com/dhis2/dhis2-android-skeleton-app](https://github.com/dhis2/dhis2-android-skeleton-app)

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[forks-shield]: https://img.shields.io/github/forks/othneildrew/Best-README-Template.svg?style=for-the-badge
[forks-url]: https://github.com/Filipcl/CCM-App/network/members
[stars-shield]: https://img.shields.io/github/stars/othneildrew/Best-README-Template.svg?style=for-the-badge
[stars-url]: https://github.com/Filipcl/CCM-App/stargazers
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://www.linkedin.com/in/filipcl/
[skeletonLogo]: https://github.com/dhis2/dhis2-android-skeleton-app/blob/master/assets/logo-launcher.png?raw=true "Skeleton logo screenshot"
[skeletonAppScreenshots]: https://github.com/dhis2/dhis2-android-skeleton-app/blob/master/assets/skeleton-app-screenshots.jpg?raw=true "Skeleton app screenshots"
[useCasesScreenshots]: https://github.com/dhis2/dhis2-android-skeleton-app/blob/master/assets/use-cases-skeleton-app-screenshots.jpg?raw=true "Use cases skeleton app screenshots"


