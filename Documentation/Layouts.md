# 8. Описание макетов

## Главное окно

В главном окне (рисунок 1) есть fragment_container для загрузки фрагментов и bottom_nav для переключения между окнами. При входе tours_recycler загружает список туров, либо empty_text выводит надпись «Туры не найдены».
Список состоит из карточек. В макете карточки тура (рисунок 2) tour_image загружает изображение из базы данных, в tour_title указывается название тура, а в tour_country — название страны. Для добавления тура в список избранных используется favorite_icon. Цена указывается в tour_price.
<div align="center">
<img width="300" height="600" alt="image" src="https://github.com/user-attachments/assets/26db0bc6-f3ef-4df2-af89-fbd7200584c8" />

Рисунок 1 - Главное окно

<img width="600" height="300" alt="image" src="https://github.com/user-attachments/assets/31e1ee93-5fcd-49db-97a7-accc8fd3e4f8" />

Рисунок 2 - Макет карточки тура
</div>

## Окно «Детали тура»
При нажатии на карточку тура в главном окне (рисунок 1) или в окне «Избранное» (рисунок 5) открывается окно деталей тура (рисунок 3). В данном окне tour_detail_image загружает изображение из базы данных, tour_detail_title отображает название тура, tour_detail_country - страну, tour_detail_price - цену, а TextView2 выводит надпись «Участники».
В поле book_button можно ввести количество участников тура. При нажатии на book_button тур бронируется, а кнопка back_button возвращает пользователя на предыдущий экран.
<div align="center">
<img width="300" height="600" alt="image" src="https://github.com/user-attachments/assets/600c8bab-5ca6-44f1-b907-b873c015517c" />

Рисунок 3 - Детали тура
</div>

## Окно «Регистрации и авторизации»

В окне регистрации и авторизации (рисунок 4) есть поле email_input для ввода почты и поле password_input для ввода пароля. Кнопка login_button предназначена для входа. При нажатии на register_link пользователь регистрируется в приложении.
<div align="center">
<img width="300" height="600" alt="image" src="https://github.com/user-attachments/assets/c6aa5677-5e99-4d64-a4f7-34094e166c37" />

Рисунок 4 - Окно регистрации и авторизации
</div>

## Окно «Брони»

В данном фрагменте виджет bookings_recycler загружает список туров окна брони (рисунок 5) в контейнер главного окна (рисунок 1).
Макет карточки брони (рисунок 6) состоит из booking_id, в котором указывается название тура, booking_status для отображения статуса, booking_participants для отображения количества участников, а booking_num выводит номер для отмены тура.
<div align="center">

<img width="300" height="600" alt="image" src="https://github.com/user-attachments/assets/20809171-af2d-46d4-bd93-771314b2fd98" />
  
Рисунок 5 - Окно брони

<img width="600" height="300" alt="image" src="https://github.com/user-attachments/assets/237d040e-65b2-487e-b622-2ccd1f365a2f" />

Рисунок 6 - Макет карточки брони
</div>

## Окно «Избранное»

В окне избранное (рисунок 7) favorites_recycler загружает список избранных туров. Макеты загружаются из макета карточки брони (рисунок 6). Если избранных туров нет, empty_text выводит надпись «Нет избранных туров».

<div align="center">
<img width="300" height="600" alt="image" src="https://github.com/user-attachments/assets/1b4a6656-cd1b-4b34-8328-879738f122a5" />

Рисунок 7 - Окно избранное
</div>

## Окно «Профиль»

В окне профиль (рисунок 8) TextView1 выводит надпись «Профиль», user_name загружает имя пользователя, user_email - адрес электронной почты, а user_role - роль пользователя в системе. С помощью кнопки logout_button можно выйти из системы.
<div align="center">
<img width="300" height="600" alt="image" src="https://github.com/user-attachments/assets/f2bad65c-5b90-4b7e-bd74-728a668f122e" />

Рисунок 8 - Окно профиль
</div>

## Окно «Управление» 

В окне управление (рисунок 9) admin_tours_recycler загружает список существующих туров. Если туров нет, empty_text выводит надпись «Нет туров для управления». При нажатии на кнопку add_tour_button открывается окно «Добавление тура» (рисунок 10).
В верхней части экрана TextView3 отображает название окна.
<div align="center">
<img width="300" height="600" alt="image" src="https://github.com/user-attachments/assets/8d4b9f64-0409-4703-b3a8-a1fcf0c40dba" />

Рисунок 9 Окно управление
</div>

## Окно «Добавление тура»

В окне добавление тура (рисунок 10) необходимо заполнить поля добавления тура. В поле title_input вводится название тура, в description_input - описание тура, в country_input - страна, в поле price_input - цена. Для загрузки изображения необходимо ввести ссылку на изображение в поле image_url_input.
Кнопка save_button сохраняет и добавляет тур, а cancel_button отменяет добавление тура.
<div align="center">
<img width="300" height="600" alt="image" src="https://github.com/user-attachments/assets/99fc5b83-1a3a-4b93-936f-7eda8ff9302e" />
  
Рисунок 10 - Окно добавление тура
</div>

## Окно «Подтверждения»

В окне «Подтверждение бронирования» (рисунок 11) admin_bookings_recycler загружает список запросов на туры. Туры в списке отображаются в виде карточек подтверждения (рисунок 12).
В макете карточки подтверждения booking_info отображает название тура, booking_status — статус тура, а booking_participants — количество участников. При нажатии на кнопку confirm_button тур подтверждается, а при нажатии на кнопку cancel_button — отклоняется. После подтверждения с помощью кнопки cancel_button можно отменить тур.
<div align="center">
<img width="300" height="600" alt="image" src="https://github.com/user-attachments/assets/c3e1bc0c-5eb6-4b3b-91c1-ebaf9e0666b9" />
  
Рисунок 11 - Окно подтверждения

<img width="600" height="300" alt="image" src="https://github.com/user-attachments/assets/d5784d1f-1f31-4d40-9249-2c6ffda59b46" />

Рисунок 12 - Макет карточки подтверждения
