###  *WAT Plan* is a full stack project of my university's departament of cybernetics schedule.
### You can download the android app's apk at https://watplan.eu.pythonanywhere.com/home

<p align="center"> 
<img src="https://imgur.com/nTTuSZb.png" height="500">
</p>
 
## How does it work?
  * ### Obtaining data   
    Scraper is a multithreaded, *Selenium* based bot, which uses *BeautifulSoup* to get and parse data from the departaments service,   
    in order to post the data to the web service described below afterwards.
    
  * ### Transfering data to the app
    To enable *WAT Plan* android app to get the desired data, I created a simple *Django* web service.   
    The service keeps track of any changes in the schedules posted to itself by scraping bots, so that *WAT Plan* is always up to date.
    
  * ### The android app itself
    It obtains data from the web service, and saves it to local sqlite storage, so that it is possible to acces previously visited schedules offline. Each time any schelude of a group is requested to open, app checks if its local version matches the one stored in the service's database.   
    
    |first startup|changing filters|
    |---|---|
    |<img src="preview/start.gif" width="285" height="600">|<img src="preview/features.gif" width="285" height="600">|
