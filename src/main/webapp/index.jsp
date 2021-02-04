<%@ page language="java" pageEncoding="UTF-8" session="true" %>

<!DOCTYPE html>
<html lang="en">
<html>
<head>
    <title>Hall 1</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/css/bootstrap.min.css"
          integrity="sha384-TX8t27EcRE3e/ihU7zmQxVncDAy5uIKz4rEkgIXeMed4M0jlfIDPvg6uqKI2xXr2" crossorigin="anonymous">

</head>
<body>
<div class="container" id="order">
    <div class="row pt-3">
        <h4>Бронирование мест на сеанс</h4>
        <table class="table table-boarded" id="hall">

        </table>
    </div>
    <div class="row float-right">
        <button type="button" class="btn btn-success" onclick="confirmOrder()">Оформить заказ</button>
    </div>
    <br>
    <div class="row">
        <button type="button" class="btn btn-primary">Доступно</button>
        <button type="button" class="btn btn-success">Выбрано</button>
        <button type="button" class="btn btn-warning">Занято</button>
        <button type="button" class="btn btn-secondary">Не доступно</button>
    </div>
</div>
<div class="container" id="buy">
    <div class="row pt-3">
        <h4>Оформление заказа</h4>
    </div>
    <div class="card">
        <div class="card-header">
            Выбранные места:
        </div>
        <div class="card-body">
            <p class="card-text" id="card"></p>
            <div class="row float-left">
                <button type="button" class="btn btn-success" onclick="buy()">Оплатить</button>
            </div>
        </div>
    </div>

</div>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-ho+j7jyWK8fNQe+A12Hb8AhRq26LrZ/JpcUGGOn+Y7RsweNrtN/tE3MoK7ZeZDyx"
        crossorigin="anonymous"></script>
<script>
    function confirmOrder() {
        $.post({
            url: location.origin + '/job4j_cinema/hall.do',
            data: {
                "action": "confirmOrder"
            },
            dataType: "json"
        }).done(function (data) {
            console.log("confirmOrder OK")
            console.log(data)
            let total = 0
            let places = ""
            for (let i = 0; i < data.length; i++) {
                places += 'Зал 1, Ряд ' + data[i][0] + ', Место ' + data[i][1] + '\n'
                total += data[i][2]
            }
            document.getElementById('card').innerText = places + '\nОщая стоимость: ' + total
            document.getElementById('order').hidden = true
            document.getElementById('buy').hidden = false
        }).fail(function () {
            console.log("confirmOrder FAIL")
            updateTable()
        })
    }

    function buy() {
        $.post({
            url: location.origin + '/job4j_cinema/hall.do',
            data: {
                "action": "buy"
            },
            dataType: "json"
        }).done(function (data) {
            console.log("buy OK")
            updateTable()
            document.getElementById('order').hidden = false
            document.getElementById('buy').hidden = true
        }).fail(function () {
            console.log("buy FAIL")
            updateTable()
            document.getElementById('order').hidden = false
            document.getElementById('buy').hidden = true
        })
    }

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
                updateTable()
            }).fail(function () {
                console.log("select FAIL")
                updateTable()
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
                updateTable()
            }).fail(function () {
                console.log("unselect FAIL")
                updateTable()
            })
        }
    }

    function updateTable() {
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
                    let bought = data[i]['places'][j]['bought']
                    let busy = data[i]['places'][j]['busy']
                    let selected = data[i]['places'][j]['selected']
                    let id = data[i]['row'] + '.' + data[i]['places'][j]['placeNo']
                    if (bought) {
                        places += '\t<td><button type=\"button\" class=\"btn btn-secondary\" disabled id =' + id + '> Ряд ' + data[i]['row'] + ', Место ' + data[i]['places'][j]['placeNo'] + '</td>\n'
                    } else if (busy) {
                        places += '\t<td><button type=\"button\" class=\"btn btn-warning\" id =' + id + ' value = "unchecked" onclick= \"checkSelect(' + id + ')\"> Ряд ' + data[i]['row'] + ', Место ' + data[i]['places'][j]['placeNo'] + '</td>\n'
                    } else if (selected) {
                        places += '\t<td><button type=\"button\" class=\"btn btn-success\"  id =' + id + ' value = "checked" onclick= \"checkSelect(' + id + ')\"> Ряд ' + data[i]['row'] + ', Место ' + data[i]['places'][j]['placeNo'] + '</td>\n'
                    } else {
                        places += '\t<td><button type=\"button\" class=\"btn btn-primary\" id =' + id + ' value = "unchecked" onclick= \"checkSelect(' + id + ')\"> Ряд ' + data[i]['row'] + ', Место ' + data[i]['places'][j]['placeNo'] + '</td>\n'
                    }
                }
                places += '</tr>\n';
            }
            places += '</tbody>\n'
            $('#hall').html(places);
        }).fail(function () {
            console.log("fail on update table")
        });
    }

    $(document).ready(function () {
        document.getElementById('buy').hidden = true
        window.setInterval(()=>updateTable(), 1000)
    })
</script>

</body>
</html>
