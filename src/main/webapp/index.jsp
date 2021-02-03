<%@ page language="java" pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="en">
<html>
<head>
    <title>Hall</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/css/bootstrap.min.css"
          integrity="sha384-TX8t27EcRE3e/ihU7zmQxVncDAy5uIKz4rEkgIXeMed4M0jlfIDPvg6uqKI2xXr2" crossorigin="anonymous">

</head>
<body>
<div class="container">
    <div class="row pt-3">
        <h4>Бронирование мест на сеанс</h4>
        <table class="table table-boarded" id="hall">

        </table>
    </div>
    <div class="row float-right">
        <button type="button" class="btn btn-success">Оплатить</button>
    </div>
</div>

<!-- Подключаем jQuery с CDN -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<!-- Подключаем Bootstrap Bundle JS с CDN -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-ho+j7jyWK8fNQe+A12Hb8AhRq26LrZ/JpcUGGOn+Y7RsweNrtN/tE3MoK7ZeZDyx"
        crossorigin="anonymous"></script>

<%--<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.11.0/umd/popper.min.js" integrity="sha384-b/U6ypiBEHpOf/4+1nzFpr53nxSS+GLCkfwBdFNTxtclqqenISfwAzpKaMNFNmj4" crossorigin="anonymous"></script>--%>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js"></script>
<script>
    function checkSelect(id) {
        let button = document.getElementById(id)
        if (button.value === 'unchecked') {
            $.post({
                url: location.origin + '/job4j_cinema/hall.do',
                data: {
                    "action": "select",
                    "id": id
                },
                dataType: "json"
            }).done(function (data) {
                console.log("select OK")
                button.value = 'checked'
                button.className = "btn btn-success"
            }).fail(function () {
                // button.innerText = button.innerText + 'место занято'
                console.log("select FAIL")
            })
        } else {
            $.post({
                url: location.origin + '/job4j_cinema/hall.do',
                data: {
                    "action": "unselect",
                    "id": id
                },
                dataType: "json"
            }).done(function (data) {
                console.log("unselect OK")
                button.value = 'unchecked'
                button.className = "btn btn-primary"
            }).fail(function () {
                console.log("unselect FAIL")
            })
        }
    }

    $(document).ready(function () {
        console.log("MSG")
        $.ajax({
            type: "GET",
            url: location.origin + '/job4j_cinema/hall.do',
            dataType: "json"
        }).done(function (data) {
            let places = '<thread>\n<tr>\n\t<th style=\"width: 120px;\">Ряд / Место</th>\n';
            for (let i = 1; i < data.length + 1; i++) {
                places += '\t<th>' + i + '</th>\n'
            }
            places += '</tr>\n</thead>\n<tbody>\n'
            for (let i = 0; i < data.length; i++) {
                places += '<tr>\n\t<th>' + data[i]['row'] + '</th>\n'
                for (let j = 0; j < data[i]['places'].length; j++) {
                    let busy = data[i]['places'][j]['busy']
                    let selected = data[i]['places'][j]['selected']
                    let id = data[i]['row'] + '.' + data[i]['places'][j]['placeNo']
                    if (busy) {
                        places += '\t<td><button type=\"button\" class=\"btn btn-secondary\" disabled id =' + id + '> Ряд ' + data[i]['row'] + ', Место ' + data[i]['places'][j]['placeNo'] + '</td>\n'
                    } else {
                        if (selected) {
                            places += '\t<td><button type=\"button\" class=\"btn btn-success\"  id =' + id + ' value = "checked" onclick= \"checkSelect(' + id + ')\"> Ряд ' + data[i]['row'] + ', Место ' + data[i]['places'][j]['placeNo'] + '</td>\n'
                        } else {
                            places += '\t<td><button type=\"button\" class=\"btn btn-primary\" id =' + id + ' value = "unchecked" onclick= \"checkSelect(' + id + ')\"> Ряд ' + data[i]['row'] + ', Место ' + data[i]['places'][j]['placeNo'] + '</td>\n'
                        }
                        //    data-toggle="tooltip" data-placement="top"
                    }
                }
                places += '</tr>\n';
            }
            places += '</tbody>\n'
            $('#hall').html(places);
        }).fail(function () {
            console.log("fail")
        });
    })
</script>

</body>
</html>
