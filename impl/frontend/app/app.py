import requests
from flask import Flask, render_template, send_from_directory, url_for, request, redirect, flash
from flask_login import LoginManager, current_user, login_user, login_required, logout_user
import os

# Usuarios
from models import users, User

# Login
from forms import LoginForm

# Register
from forms import RegisterForm

app = Flask(__name__, static_url_path='')
login_manager = LoginManager()
login_manager.init_app(app)  # Para mantener la sesión

# Configurar el secret_key. OJO, no debe ir en un servidor git público.
# Python ofrece varias formas de almacenar esto de forma segura, que
# no cubriremos aquí.
app.config[
    'SECRET_KEY'] = 'qH1vprMjavek52cv7Lmfe1FoCexrrV8egFnB21jHhkuOHm8hJUe1hwn7pKEZQ1fioUzDb3sWcNK1pJVVIhyrgvFiIrceXpKJBFIn_i9-LTLBCc4cqaI3gjJJHU6kxuT8bnC7Ng'


@app.route('/static/<path:path>')
def serve_static(path):
    return send_from_directory('static', path)


@app.route('/')
def index():
    return render_template('index.html')


# @app.route('/a', methods=['GET', 'POST'])
# def prueba():
#     payload = {'key1': 'value1'}
#     r = requests.post("http://localhost:8080/Service/prueba", json=payload)
#     return r.text

app.config["IMAGE_UPLOADS"] = "/Users/jtamb/Desktop/Trabajos/Cuarto/SSDD/ssdd-21-22/impl/frontend/app/static/images/uploads"
app.config["ALLOWED_VIDEO_EXTENSIONS"] = ["MP4", "AVI", "MKV"]
FILE_UPLOAD_MAX_MEMORY_SIZE = int(1024 * 1024 * 1024 * 1024)


def allowed_video(filename):
    if not "." in filename:
        return False
    ext = filename.rsplit(".", 1)[1]
    if ext.upper() in app.config["ALLOWED_VIDEO_EXTENSIONS"]:
        return True
    else:
        return False


@app.route('/uploadvideo', methods=['GET', 'POST'])
def uploadvideo():
    if request.method == "POST":
        if request.files:
            video = request.files["video"]
            print(video.filename)
            if video.filename == "":
                error = "Debe tener un nombre"
                return render_template('uploadvideoscreen.html', error=error)
            elif allowed_video(video.filename):
                # video.save(os.path.join(app.config['IMAGE_UPLOADS'], video.filename))
                r = requests.post('http://localhost:8080/Service/sendvideos', files={'file': video})
                print("Image saved %s", r.status_code)
                return render_template('uploadvideoscreen.html')
            else:
                error = "No está en el formato correcto"
                return render_template('uploadvideoscreen.html', error=error)
    return render_template('uploadvideoscreen.html')


# @app.route('/uploadingvideo', methods=['GET', 'POST'])
# def uploadingvideo():
#     if request.method == 'POST':
#         # check if the post request has the file part
#         if 'file' not in request.files:
#             flash('No file part')
#             return redirect(request.url)
#         file = request.files['file']
#         # if user does not select file, browser also
#         # submit a empty part without filename
#         if file.filename == '':
#             flash('No selected file')
#             return redirect(request.url)
#         if file and allowed_file(file.filename):
#             filename = secure_filename(file.filename)
#             file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))


@app.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        return redirect(url_for('index'))
    else:
        error = None
        form = LoginForm(request.form)
        if request.method == "POST" and form.validate_on_submit():
            payload = {'email': form.email.data, 'password': form.password.data}
            r = requests.post('http://localhost:8080/Service/checkLogin', json=payload)

            if r.status_code == 200:
                user = User.get_user(form.email.data.encode('utf-8'))
                if user is None:
                    user = User(users.__len__() + 1, r.json()['name'], form.email.data.encode('utf-8'),
                                form.password.data.encode('utf-8'))
                    users.append(user)
                    app.logger.info("Usuarios --> %s", users)
                login_user(user, remember=form.remember_me.data)
                return redirect(url_for('index'))
            else:
                error = 'Invalid Credentials. Please try again.'
            # if form.email.data != 'admin@um.es' or form.password.data != 'admin':
            #     error = 'Invalid Credentials. Please try again.'
            # else:
            #     user = User(1, 'admin', form.email.data.encode('utf-8'),
            #                 form.password.data.encode('utf-8'))
            #     users.append(user)
            #     login_user(user, remember=form.remember_me.data)
            #     return redirect(url_for('index'))

        return render_template('login.html', form=form, error=error)


@app.route('/register', methods=['GET', 'POST'])
def register():
    form = RegisterForm(request.form)
    if request.method == "POST" and form.validate_on_submit():
        payload = {'email': form.email.data, 'name': form.username.data, 'password': form.password.data}
        r = requests.post('http://localhost:8080/Service/prueba', json=payload)
        app.logger.info("domingo domigno %s", r.status_code)
        if r.status_code == 201:
            user = User(users.__len__() + 1, form.username, form.email.data.encode('utf-8'),
                        form.password.data.encode('utf-8'))
            app.logger.info("AAAAAAAA %s", user)
            users.append(user)
            login_user(user, remember=form.remember_me.data)
            return redirect(url_for('index'))
        else:
            error = 'Invalid Credentials. Please try again.'
    return render_template('signup.html', form=form, error=error)


@app.route('/profile')
@login_required
def profile():
    return render_template('profile.html')


@app.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('index'))


@login_manager.user_loader
def load_user(user_id):
    for user in users:
        if user.id == int(user_id):
            return user
    return None


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')
