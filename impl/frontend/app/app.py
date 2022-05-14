import os

import requests, json
from flask import Flask, render_template, send_from_directory, url_for, request, redirect, flash, json
from flask_login import LoginManager, current_user, login_user, login_required, logout_user

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


app.config[
    "IMAGE_UPLOADS"] = "/Users/jtamb/Desktop/Trabajos/Cuarto/SSDD/ssdd-21-22/impl/frontend/app/static/images/uploads"
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


@app.route('/consultvideos', methods=['GET'])
def consultvideos():
    r = requests.get("http://localhost:8080/Service/users/" + current_user.get_id() + "/consultvideos");
    vids = r.json()
    return render_template('consultvideosscreen.html', videos=vids)


@app.route('/videoinformation/<video_id>', methods=['GET'])
@login_required
def consultvideo(video_id=0):
    r = requests.get("http://localhost:8080/Service/users/" + video_id + "/getVideo");
    vid = r.json()

    for file in os.scandir('./static/images'):
        os.remove(file.path)

    r = requests.get('http://localhost:8080/Service/users/' + current_user.get_id() + '/video/' + video_id)

    if not len(r.content) == 0:
        splits = r.content.split("\",\"".encode())

        i = 0
        for s in splits:
            with open('./static/images/file' + str(i) + '.jpg', 'wb') as f:
                f.write(splits[i])
            i = i + 1
        images = os.listdir('./static/images')
        return render_template('consultvideo.html', video=vid, images=images)
    else:
        return render_template('consultvideo.html', video=vid)


@app.route('/photos/<filename>')
def send_image(filename):
    return send_from_directory("static/images", filename)


@app.route('/videoinformation/removevideo/<video_id>', methods=['GET'])
@login_required
def removevideo(video_id=0):
    r = requests.get("http://localhost:8080/Service/users/" + video_id + "/removeVideo");
    return redirect(url_for('consultvideos'))


@app.route('/uploadvideo', methods=['GET', 'POST'])
@login_required
def uploadvideo():
    error = None
    if request.method == "POST":
        if request.files:
            video = request.files["video"]
            if video.filename == "":
                error = "Debe tener un nombre"
            elif allowed_video(video.filename):
                r = requests.post(
                    "http://localhost:8080/Service/users/" + current_user.get_id() + "/" + video.filename + "/video",
                    data=video)
            else:
                error = "No está en el formato correcto"
    return render_template('uploadvideoscreen.html', error=error)


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
                    user = User(int(r.json()['id']), r.json()['name'], form.email.data.encode('utf-8'),
                                form.password.data.encode('utf-8'))
                    users.append(user)
                login_user(user, remember=form.remember_me.data)
                return redirect(url_for('index'))
            else:
                error = 'Invalid Credentials. Please try again.'

        return render_template('login.html', form=form, error=error)


@app.route('/register', methods=['GET', 'POST'])
def register():
    form = RegisterForm(request.form)
    error = None
    if request.method == "POST" and form.validate_on_submit():
        payload = {'email': form.email.data, 'name': form.username.data, 'password': form.password.data}
        r = requests.post('http://localhost:8080/Service/register', json=payload)
        if r.status_code == 201:
            user = User(int(r.json()['id']['string']), form.username, form.email.data.encode('utf-8'),
                        form.password.data.encode('utf-8'))
            users.append(user)
            login_user(user, remember=form.remember_me.data)
            return redirect(url_for('index'))
        else:
            error = 'Invalid Credentials. Please try again.'
    return render_template('signup.html', form=form, error=error)


@app.route('/profile', methods=['GET'])
@login_required
def profile():
    r = requests.get('http://localhost:8080/Service/users/' + current_user.get_id())
    return render_template('profile.html', id=r.json()['id']['string'], email=r.json()['email']['string'],
                           token=r.json()['TOKEN']['string'], name=r.json()['name']['string'],
                           visits=r.json()['visits']['string'], videos=r.json()['videos']['string'])


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
