from rest_framework.viewsets import GenericViewSet
from .models import Group
from rest_framework.serializers import ModelSerializer


class SemesterSerializer(ModelSerializer):

    class Meta:
        model = Group
        lookup_field = 'name'
        fields = '__all__'


class SemesterViewSet(GenericViewSet):
    queryset = Semester.objects.all()
    serializer_class = SemesterSerializer