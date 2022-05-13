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
app.config['SECRET_KEY'] = 'qH1vprMjavek52cv7Lmfe1FoCexrrV8egFnB21jHhkuOHm8hJUe1hwn7pKEZQ1fioUzDb3sWcNK1pJVVIhyrgvFiIrceXpKJBFIn_i9-LTLBCc4cqaI3gjJJHU6kxuT8bnC7Ng'


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
    
    print("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB")
    print("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA: ", current_user.get_id())
    
    r = requests.get("http://localhost:8080/Service/users/" + current_user.get_id() + "/consultvideos");

    print("Morango %s", r.json())

    vids = r.json()

    return render_template('consultvideosscreen.html', videos=vids)


@app.route('/videoinformation/<video_id>', methods=['GET'])
@login_required
def consultvideo(video_id=0):
    
    r = requests.get("http://localhost:8080/Service/users/" + video_id + "/getVideo");
    vid = r.json()
    
    return render_template('consultvideo.html', video=vid)

@app.route('/videoinformation/removevideo/<video_id>', methods=['GET'])
@login_required
def removevideo(video_id=0):
    
    print("VNNNNNNNNNNNNNNNNNNNNN")
    r = requests.get("http://localhost:8080/Service/users/" + video_id + "/removeVideo");

    print("VNNNNNNNNNNNNNNNNNNNNN")

    return redirect(url_for('consultvideos'))

@app.route('/uploadvideo', methods=['GET', 'POST'])
@login_required
def uploadvideo():
    error = None
    if request.method == "POST":
        if request.files:
            video = request.files["video"]
            print(video.filename)
            if video.filename == "":
                error = "Debe tener un nombre"
            elif allowed_video(video.filename):
                r = requests.post("http://localhost:8080/Service/users/" + current_user.get_id() + "/" + video.filename + "/video", data=video)
                print("Image saved %s", r.status_code)
            else:
                error = "No está en el formato correcto"
    return render_template('uploadvideoscreen.html', error=error)


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
            print(r.json())

            if r.status_code == 200:
                user = User.get_user(form.email.data.encode('utf-8'))
                print("Tiene algo? %s", user)
                if user is None:
                    user = User(int(r.json()['id']), r.json()['name'], form.email.data.encode('utf-8'),
                                form.password.data.encode('utf-8'))
                    users.append(user)
                    print(user.get_id())
                    app.logger.info("Usuarios --> %s", users)
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
        r = requests.post('http://localhost:8080/Service/prueba', json=payload)
        app.logger.info("domingo domigno %s - %s", r.status_code, r.json()['id']['string'])
        if r.status_code == 201:
            user = User(int(r.json()['id']['string']), form.username, form.email.data.encode('utf-8'),
                        form.password.data.encode('utf-8'))
            app.logger.info("AAAAAAAA %s", user)
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


@app.route('/photos', methods=['GET'])
def photos():
    images = os.listdir('./static/images')
    return render_template('photos.html', images=images)


@app.route('/photos/<filename>')
def send_image(filename):
    return send_from_directory("static/images", filename)


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
