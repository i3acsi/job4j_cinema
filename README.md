[GIT REPOSITORY](https://github.com/i3acsi/job4j_cinema.git)

[![Build Status](https://travis-ci.org/i3acsi/job4j_cinema.svg?branch=master)](https://travis-ci.org/i3acsi/job4j_cinema)
[![codecov](https://codecov.io/gh/i3acsi/job4j_dreamjob/branch/master/graph/badge.svg?token=9RQSL2GZ16)](https://codecov.io/gh/i3acsi/job4j_dreamjob)

Проект  Сервис - Кинотеатр.
<br>
веб сайт для покупки билетов в кинотеатр.

В системе представлны следующие модели:
 
 + пользователи
    + роли
 + места
- - -

<div>
При посещении главной страницы, в системе можно залогиниться или зарегистрироваться: 
<br>

![alt text](img/greetingPage.png "")
</div>
 
 <div>
<br>
 На станице есть валидация введенных данных:
 <br>
 пароли - по длинне и по эквивалентности (при регистрации),
 <br>
 email - по regexp,
 <br>
 имя - по длинне
 <br>
 
 ![alt text](img/inputValidation.png "")
 </div>
 
 <div>
<br>
 При успешной регистрации нового пользователя, информация об этом отобразится на странице.
 <br>
 Если произошла ошибка регистрации (например пользователь с таким email уже есть в системе),
 <br>
 информация об этом так же отобразится.
 <br>
    
  ![alt text](img/regOk.png "")
  </div>
  
  <div>
<br>
  При попытке залогиниться в системе (как и при регистрации), на сервер отправляется асинхронный запрос.
  <br>
  Дальнейшее поведение зависит от ответа сервера.
  <br>
  Напимер, если пользователь с введеной парой email-password не найден, информация об этом отобразится.
  <br>
      
  ![alt text](img/loginFail.png "")
  </div>
  
  <div>
  <br>
  Если введеные данные верны, у сесси устанавливается аттрибут user - для корректной работы фильта,
  <br>
  и со станицы приветствия, пользователь перенаправляется на страицу кинозала с местами.
  <br>
  Места отобразятся цветом, в соответствии со своим статусом.
  <br>
  Подсказка по статусам есть на странице
  <br>
      
 ![alt text](img/hallFirst.png "")
 <br>
 Когда на соответствующий сервлет приходит get запрос для получения информации о местах,
 <br>
 на базу данных приходит запрос по состоянию мест для этого зала, и на основании ответа,
 <br>
 формируется ConcurrentHashMap, мапа буферизует текущее состояние мест зала 
 <br>
 (т.к. оно меняется только при покупке соответствующих мест).
 <br>
 Мапа хранит состояние selected, bought, account_id
 <br>
 т.е., если у места состояние bought - оно не доступно, если состояние selected,
 <br>
 его может сделать unselected или bought только пользователь с соответствующим account_id.
 <br> 
 Для более удобной работы с данными на странице, в ответе от сервера использую DTO.
 <br> 
 Т.е. данные из мапы преобразую в лист DTO, который затем преобразуется в json.
 <br> 

 </div>
  
 <div>
 <br>
 При выборе мест, они становятся зеленого цвета
  
 ![alt text](img/hallSelect.png "")

 <br>
 При этом у другого пользователя, в этот момент осуществляющего выбор мест, эти места становятся желтыми, 
 <br>
(не доступными для выбора в данный момент)
  
 ![alt text](img/hallSelectAnotherUser.png "")

 Это сделано спомощью window.setInterval() к которой привязана функция обновления отображения мест - updateTable(). 
 <br>
 updateTable() делает асинхронный запрос на сервер, он на осовании мапы в ответ отправляет json
 <br>
 и распарсив json, функция формирует html код, который размещает на странице.
 <br>
 сама же мапа сверяется с БД только если кто-то совершил покупку.
 <br>
 </div>

<div>
<br>
 Когда все нужные места выбраны, нажимаем - "оформить заказ".
 <br>
 На сервер отправляется асинхронный запрос с id клиента и номером зала, в ответ мы получаем
 <br>
 лист интовых массивов, котроый мы интерпретируем как список мест (ряд/место) и их цену.
 <br>
 На основании полученных данных, выводим информацию о покупаемых местах с итоговой суммой.
 <br>
 
    
 ![alt text](img/order.png "")
 </div>
 
 <div>
  <br>
  Нажав "оплатить", мы опять отправляем на сервер асинхронный запрос с id клиента и номером зала,
  <br>
  но в этот раз мы осуществляем покупку.
  <br>
  Все запросы на эти действия приходят на один сервлет методу post,
  <br>
  но для каждого действия, в параметрах метода, передается определенный action.
  <br>
  В соответствии с переданным параметром action, сервлет делает нужное нам действие.
  <br>
  В данном случае, при покупке, мы опять обходим мапу, чтобы определить выбранные пользователем места,
  <br>
  чтобы затем сделать запрос в БД, и пометить эти мета как купленные, обновить состояние
  <br>
  мапы в соответствии с данными в БД.
  <br>
     
  ![alt text](img/boughtOk.png "")
  </div>
 
 ---
  Используемые технологии:
 1. Apache Tomcat — контейнер сервлетов
 2. Java servlets, MVC
 3. JSP, JSTL
 4. HTML, CSS, JS, Bootstrap, jquery ajax
 5. PostgreSQL 
 6. JUnit, Mockito, PowerMock
 7. Git, Travis CI, CodeCov